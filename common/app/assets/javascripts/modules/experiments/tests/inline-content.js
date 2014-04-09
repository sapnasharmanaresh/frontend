define([
    'common/utils/detect',
    'common/modules/experiments/inline-content'
], function(
    detect,
    inlineElements
    ) {

    return function() {

        this.id = 'InlineContent';
        this.description = 'Test whether inlining content in the story package improves page views per session and/or CTR on the story trails.';
        this.start = '2014-04-10';
        this.expiry = '2014-4-16';
        this.audience = 0.3;
        this.audienceOffset = 0.2;
        this.audienceCriteria = 'Article page with a story package and >=2 inline slots';
        this.successMeasure = 'PVPS, trail CTR and onward journey rate from tested pages';
        this.idealOutcome = 'Marked improvment in success measure';
        this.dataLinkNames = 'inline story content';
        this.canRun = function(config) {
            return detect.getBreakpoint() !== 'mobile' &&
                   config.page.contentType === 'Article' &&
                   config.page.hasStoryPackage &&
                   document.querySelectorAll('.article-body .slot').length >= 2 &&
                   document.querySelectorAll('.article-body .img, .article-body video').length < 3;
        };
        this.variants = [
            {
                id: 'inlineElements',
                test: function () {
                    inlineElements.init();
                }
            },
            {
                id: 'control',
                test: function () {

                }
            }
        ];
    };
});