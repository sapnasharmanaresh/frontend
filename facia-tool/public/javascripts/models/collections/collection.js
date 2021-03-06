/* global _: true, humanized_time_span: true */
define([
    'config',
    'knockout',
    'modules/vars',
    'utils/as-observable-props',
    'utils/populate-observables',
    'modules/authed-ajax',
    'models/group',
    'models/collections/article',
    'modules/content-api',
    'js!humanized-time-span'
], function(
    config,
    ko,
    vars,
    asObservableProps,
    populateObservables,
    authedAjax,
    Group,
    Article,
    contentApi
    ) {
    function Collection(opts) {

        if (!opts || !opts.id) { return; }

        this.id = opts.id;
        this.raw = undefined;
        this.groups = this.createGroups(opts.groups);
        this.alsoOn = opts.alsoOn || [];

        // properties from the config, about this collection
        this.configMeta   = asObservableProps([
            'displayName',
            'uneditable']);
        populateObservables(this.configMeta, opts);

        // properties from the collection itself
        this.collectionMeta = asObservableProps([
            'displayName',
            'href',
            'lastUpdated',
            'updatedBy',
            'updatedEmail']);

        this.state  = asObservableProps([
            'lastUpdated',
            'hasConcurrentEdits',
            'collapsed',
            'hasDraft',
            'pending',
            'editingConfig',
            'count',
            'timeAgo']);

        this.setPending(true);
        this.load();
    }

    Collection.prototype.setPending = function(bool) {
        this.state.pending(!!bool);
    };

    Collection.prototype.isPending = function() {
        return !!this.state.pending();
    };

    Collection.prototype.createGroups = function(groupNames) {
        var self = this;

        return _.map(_.isArray(groupNames) ? groupNames : [undefined], function(name, index) {
            return new Group({
                group: index,
                name: name,
                parent: self,
                parentType: 'Collection',
                omitItem: self.drop.bind(self)
            });
        }).reverse(); // because groupNames is assumed to be in ascending order of importance, yet should render in descending order
    };

    Collection.prototype.toggleCollapsed = function() {
        this.state.collapsed(!this.state.collapsed());
        this.closeAllArticles();
    };

    Collection.prototype.toggleEditingConfig = function() {
        this.state.editingConfig(!this.state.editingConfig());
    };

    Collection.prototype.reset = function() {
        this.closeAllArticles();
        this.state.editingConfig(false);
        this.load();
    };

    Collection.prototype.publishDraft = function() {
        this.processDraft(true);
    };

    Collection.prototype.discardDraft = function() {
        this.processDraft(false);
    };

    Collection.prototype.processDraft = function(goLive) {
        var self = this;

        this.state.hasDraft(false);
        this.setPending(true);
        this.closeAllArticles();

        authedAjax.request({
            type: 'post',
            url: vars.CONST.apiBase + '/collection/'+ (goLive ? 'publish' : 'discard') + '/' + this.id
        })
        .then(function() {
            self.load()
            .then(function(){
                if (goLive) {
                    vars.model.deferredDetectPressFailure();
                }
            });
        });
    };

    Collection.prototype.drop = function(item) {
        this.setPending(true);

        authedAjax.updateCollections({
            remove: {
                collection: this,
                item:       item.id(),
                live:       vars.state.liveMode(),
                draft:     !vars.state.liveMode()
            }
        })
        .then(function() {
            if(vars.state.liveMode()) {
                vars.model.deferredDetectPressFailure();
            }
        });
    };

    Collection.prototype.load = function(opts) {
        var self = this;

        opts = opts || {};

        if (opts.isRefresh && this.isPending()) { return; }

        if (this.configMeta.uneditable()) { return; }

        return authedAjax.request({
            url: vars.CONST.apiBase + '/collection/' + this.id
        })
        .done(function(raw) {
            if (opts.isRefresh && self.isPending()) { return; }

            self.setPending(false);

            if (!raw) { return; }

            self.state.hasConcurrentEdits(false);

            if (raw.lastUpdated !== self.state.lastUpdated()) {
                self.populate(raw);
            }

            if (!self.state.editingConfig()) {
                populateObservables(self.collectionMeta, raw);
                self.collectionMeta.updatedBy(raw.updatedEmail === config.email ? 'you' : raw.updatedBy);
                self.state.timeAgo(self.getTimeAgo(raw.lastUpdated));
            }
        })
        .fail(function() {
            self.setPending(false);
        });
    };

    Collection.prototype.hasOpenArticles = function() {
        return _.reduce(this.groups, function(hasOpen, group) {
            return hasOpen || _.some(group.items(), function(article) { return article.state.open(); });
        }, false);
    };

    Collection.prototype.populate = function(raw) {
        var self = this,
            list;

        this.setPending(false);

        raw = raw ? raw : this.raw;
        this.raw = raw;
        if (!raw) { return; }

        this.state.hasDraft(_.isArray(raw.draft));

        if (this.hasOpenArticles()) {
            this.state.hasConcurrentEdits(raw.updatedEmail !== config.email && self.state.lastUpdated());
            return;
        }

        list = vars.state.liveMode() ? raw.live : raw.draft || raw.live || [];

        _.each(this.groups, function(group) {
            group.items.removeAll();
        });

        _.each(list, function(item) {
            (_.find(self.groups, function(g){
                return (parseInt((item.meta || {}).group, 10) || 0) === g.group;
            }) || self.groups[0])
            .items.push(
                new Article(_.extend(item, {
                    parent: self,
                    parentType: 'Collection'
                }))
            );
        });

        this.state.lastUpdated(raw.lastUpdated);
        this.state.count(list.length);

        this.decorate();
    };

    Collection.prototype.closeAllArticles = function() {
        _.each(this.groups, function(group) {
            _.each(group.items(), function(item) {
                item.close();
            });
        });
    };

    Collection.prototype.decorate = function() {
        _.each(this.groups, function(group) {
            contentApi.decorateItems(group.items());
        });
    };

    Collection.prototype.refresh = function() {
        if (this.isPending()) { return; }

        this.load({
            isRefresh: true
        });
    };

    Collection.prototype.refreshSparklines = function() {
        _.each(this.groups, function(group) {
            _.each(group.items(), function(item) {
                item.sparkline();
            });
        });
    };

    Collection.prototype.saveMeta = function() {
        var self = this;

        this.state.editingConfig(false);
        this.setPending(true);

        authedAjax.request({
            url: vars.CONST.apiBase + '/collectionmeta/' + this.id,
            type: 'post',
            data: JSON.stringify({
                displayName: this.collectionMeta.displayName(),
                href: this.collectionMeta.href()
            })
        })
        .then(function(){
            self.load();
        });
    };

    Collection.prototype.getTimeAgo = function(date) {
        return date ? humanized_time_span(date) : '';
    };

    return Collection;
});
