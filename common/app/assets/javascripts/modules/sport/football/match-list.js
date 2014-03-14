define([
    'common/modules/component',
    'common/$',
    'bonzo',
    'common/utils/mediator'
], function(
    component,
    $,
    bonzo,
    mediator
) {

    var MatchList = function(competition) {
        this.endpoint += ['football/api', competition, 'live-matches'].filter(function(e) { return e; }).join('/') +'.json';
        this.autoupdate = 30 * 1000;
    };
    component.define(MatchList);

    MatchList.prototype.endpoint = '/';

    MatchList.prototype.prerender = function() {
        var elem = this.elem;

        $('.football-team__form', elem).remove();
        $('.date-divider', elem).remove();
        $(this.elem).addClass('table--small');
        bonzo.create('<caption class="table__caption">Scores</caption>').map(function(el) {
            $('.football-matches', elem).prepend(el);
        });
    };

    return MatchList;

}); // define