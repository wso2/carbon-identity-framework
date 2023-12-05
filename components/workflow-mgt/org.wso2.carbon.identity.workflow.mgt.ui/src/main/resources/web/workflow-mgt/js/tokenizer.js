// Input Tokenizer
// Author: Don McCurdy
// Date: July, 2012

(function ($) {
    var tokenizer = 'tokenizer';

    var Tokenizer = function (argElement, argOpts) {

        // PRIVATE VARS
        var
                input,
                list,
                wrap,
                suggestions,
                eventQueue = $({}),
                options = $.extend({
                    xContent: '&times;',
                    namespace: 'tknz',
                    label: 'Tags:',
                    placeholder: '',
                    separators: [',', ' ', '.'],
                    callback: null,
                    source: null,
                    allowUnknownTags: true,
                    numToSuggest: 5,
                    onclick: null
                }, argOpts);

        // PRIVATE METHODS
        var init, buildHTML, bindEvents, push, pop, remove, get, empty,
                destroy, callback, suggest, getMatch, tryPush, escapeRegExp;

        init = function () {
            input = argElement;
            buildHTML();
            bindEvents();
        };
        buildHTML = function () {
            var ns = options.namespace;
            list = $('<div class="'+ns+'-list"></div>');
            if (options.placeholder) { input.attr('placeholder', options.placeholder); }
            suggestions = $('<div class="'+ns+'-suggest"><ul></ul></div>');
            wrap = input
                    .addClass(ns+'-input')
                    .wrap('<span class="'+ns+'-input-wrapper"></span>')
                    .parent()
                    .wrap('<div class="'+ns+'-wrapper"></div>')
                    .parent()
                    .prepend(list)
                    .prepend('<span class="'+ns+'-wrapper-label">'+options.label+'</span>')
                    .find('.'+ns+'-input-wrapper').append(suggestions).end();
        };
        bindEvents = function () {
            var ns = options.namespace;
            wrap
                    .on('focus', 'input', function () {  // On focus, stylize wrapper.
                            wrap.addClass(ns+'-focus');
                        }).on('blur', 'input', function () { // On blur, un-stylize.
                                  wrap.removeClass(ns+'-focus');
                                  eventQueue.delay(200).queue().push(function () {
                                      tryPush(input.val());
                                      $.dequeue(this);
                                  });
                              }).on('keydown', 'input', function (event) { // Backspace handler.
                                        event = event || window.event;
                                        event.which = event.which || event.keyCode || event.charCode;
                                        if (event.which === 8 && !input.val()) {
                                            pop();
                                            callback();
                                            return;
                                        }
                                        var
                                                selectClass = ns+'-sel',
                                                selected = suggestions.find('.'+selectClass);

                                        if (event.which === 38) { // Up
                                            event.preventDefault();
                                            if (selected.length) {
                                                selected.removeClass(selectClass)
                                                        .prev('li').add(selected.siblings().last()).eq(0).addClass(selectClass);
                                            } else {
                                                suggestions.find('li').last().addClass(selectClass);
                                            }
                                        } else if (event.which === 40) { // Down
                                            event.preventDefault();
                                            if (selected.length) {
                                                selected.removeClass(selectClass)
                                                        .next('li').addClass(selectClass);
                                            } else {
                                                suggestions.find('li').first().addClass(selectClass);
                                            }
                                        }
                                    }).on('keypress', 'input', function (event) { // Input listener to create tokens.
                                              if (options.separators.indexOf(String.fromCharCode(event.which)) > -1 || event.which === 13) {
                                                  event.preventDefault();
                                                  tryPush(input.val());
                                              }
                                          }).on('keyup', 'input', function (event) {
                                                    event = event || window.event;
                                                    event.which = event.which || event.keyCode || event.charCode;
                                                    if (event.which === 38 || event.which === 40) { return; }
                                                    if ($.isArray(options.source)) { // Autosuggest from list
                                                        suggest(options.source);
                                                    } else if (options.source) { // Autosuggest from function
                                                        options.source(input.val(), suggest);
                                                    }
                                                }).on('click', function () {	// On click, focus the input.
                                                          input.focus();
                                                      }).on('click', '.'+ns+'-token-x', function (event) {
                                                                event.stopPropagation();
                                                                $(this).closest('.'+ns+'-token').remove();
                                                                callback();
                                                            }).on('click', '.'+ns+'-suggest-li', function () {
                                                                      $.queue(eventQueue, []); // cancel blur event
                                                                      input.val('');
                                                                      push($(this).text());
                                                                      suggest([]);
                                                                      callback();
                                                                  }).on('click', '.'+ns+'-token', function (event) {
                                                                            if (options.onclick) {
                                                                                event.stopPropagation();
                                                                                options.onclick($(this).children('.'+ns+'-token-label').text());
                                                                            }
                                                                        });
        };

        tryPush = function (value) {
            var match = getMatch();
            if (value && (options.allowUnknownTags || match)) {
                push(match || value);
                input.val('');
                callback();
            }
            suggest([], '');
        };
        push = function (value) {
            var
                    ns = options.namespace,
                    pre = ns+'-token',
                    token = '<div class="'+pre+'" data-token="'+value+'">'+
                            '<span class="'+pre+'-label">'+value.trim()+'</span>'+
                            '<span class="'+pre+'-x">'+options.xContent+'</span>'+
                            '</div>';
            list.append(token);
            return input;
        };
        pop = function () {
            return list.children().last().detach().data('token') || null;
        };
        remove = function (value) {
            var tokens = list.children().filter(function() {
                return $(this).data('token') == value;
            }).detach();
            return tokens.length > 0 ? (tokens.length == 1 ? tokens.data('token') : tokens.length) : null;
        };
        empty = function () {
            list.empty();
            return input;
        };
        get = function () {
            var
                    i,
                    tokenList = [],
                    tokens = list.children();
            for (i = 0; i < tokens.length; i++) {
                tokenList.push(tokens.eq(i).data('token').toString());
            }
            return tokenList;
        };
        destroy = function () {
            wrap.after(input).remove();
            if (options.placeholder) { input.attr('placeholder', ''); }
            return input.removeClass(options.namespace+'-input');
        };
        callback = function () {
            (options.callback || $.noop)(input);
            return input;
        };
        suggest = function (words, word) {
            word = word === undefined ? input.val() : word;
            var
                    i,
                    ns = options.namespace,
                    re1 = new RegExp(escapeRegExp(word), 'i'),
                    re2 = new RegExp('^'+escapeRegExp(word)+'$', 'i'),
                    limit = options.numToSuggest || 1000,
                    list = [];
            for (i = 0; word && i < words.length && list.length < limit; i++) {
                if (!words[i].match(re1)) { continue; }
                list.push('<li class="'+ns+'-suggest-li'+
                          (words[i].match(re2) ? ' '+ns+'-sel' : '')+'">'+words[i]+'</li>');
            }
            suggestions.children('ul')
                    .html(list.join(''))
                    .end()
                    [list.length ? 'addClass' : 'removeClass']('.'+ns+'-vis');
        };
        getMatch = function () {
            return suggestions.find('.'+options.namespace+'-sel').eq(0).text();
        };
        escapeRegExp = function (str) {
            return str.replace(/[\-\[\]\/\{\}\(\)\*\+\?\.\\\^\$\|]/g, "\\$&");
        };

        init (argElement);
        return {
            push: push,
            pop: pop,
            remove: remove,
            empty: empty,
            get: get,
            destroy: destroy,
            callback: callback
        };
    };


    // PUBLIC METHODS
    var methods = {
        init: function( options ) {
            if (this[0].nodeName !== 'INPUT') {
                $.error('Tokenizer requires an <input type="text"> tag.');
                return this;
            }
            return this.data(tokenizer, Tokenizer(this, options));
        },
        push: function(value) {
            return this.data(tokenizer).push(value);
        },
        pop: function() {
            return this.data(tokenizer).pop();
        },
        remove: function(value) {
            return this.data(tokenizer).remove(value);
        },
        empty : function() {
            return this.data(tokenizer).empty();
        },
        get: function () {
            return this.data(tokenizer).get();
        },
        destroy: function () {
            return this.data(tokenizer).destroy();
        },
        callback: function () {
            return this.data(tokenizer).callback();
        }
    };


    // EXPORT PLUGIN
    $.fn[tokenizer] = function( method ) {
        if ( methods[method] ) {
            if (!this.data(tokenizer)) { $.error('Cannot call "'+method+'" - Tokenizer not initialized.'); }
            return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
        } else if ( typeof method === 'object' || ! method ) {
            return methods.init.apply( this, arguments );
        } else {
            $.error( 'Unknown tokenizer method ' +  method + '.' );
        }
    };
}(jQuery));