define([
    'common/common',
    'common/$',
    'qwery',
    'bonzo',
    'common/modules/onward/slot-controller',
    'common/modules/analytics/register'
], function (
    common,
    $,
    qwery,
    bonzo,
    SlotController,
    register
    ) {

    var componentName = 'inline-component';

    function replaceClasses(el) {
        var classes = el.getAttribute('class');
        if (classes) {
            el.setAttribute('class', classes.replace(/item/g,'inline-story'));
        }
    }

    function moveItem(item) {
        var newItem = item.cloneNode(true);
        bonzo(newItem.getElementsByClassName('item__meta')[0]).addClass('js-append-commentcount');
        replaceClasses(newItem);
        qwery('*', newItem).forEach(replaceClasses);
        var storySlot = SlotController.getSlot('story');
        bonzo(newItem)
            .attr('data-component', componentName)
            .appendTo(storySlot);
    }

    return {
        init: function() {

            if (!window.guardian.config.page.hasStoryPackage) { return false; }

            var inlineables = $('.item--gallery, .item--video', '.more-on-this-story');

            if (inlineables.length > 0) {
                register.begin(componentName);
                for (var i = 0; i < Math.min(2, inlineables.length); i++) {
                    moveItem(inlineables[i]);
                }
                register.end(componentName);
            }
        }
    };
});