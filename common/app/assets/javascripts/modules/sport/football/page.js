define([
    'common/$',
    'common/utils/config'
], function(
    $,
    config
) {

    function isMatch(yes) {
        var teams = config.referencesOfType('paFootballTeam'),
            footballMatch = config.page.footballMatch;

        if (footballMatch ||
            ((config.hasTone("Match reports") || config.page.isLiveBlog) && teams.length === 2)) {
            return yes ? yes(footballMatch || {
                date: config.webPublicationDateAsUrlPart(),
                teams: teams
            }) : true;
        }
    }

    function isCompetition(yes) {
        var competition = ($('.js-football-competition').attr('data-link-name') || '').replace('keyword: ', '');
        if (competition) {
            return yes ? yes(competition) : true;
        }
    }

    function isClockwatch(yes) {
        if (config.hasSeries('Clockwatch')) {
            return yes ? yes(config.page.isLive) : true;
        }
    }

    return {
        isMatch: isMatch,
        isCompetition: isCompetition,
        isClockwatch: isClockwatch
    };

}); // define

