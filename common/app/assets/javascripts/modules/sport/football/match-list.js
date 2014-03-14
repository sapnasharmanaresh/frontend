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

var MatchList = function(type, filter) {
    this.endpoint += ['football', filter, type].filter(function(e) { return e; }).join('/') +'.json';
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