define(['angularAMD',
    'app/routingConfig',
    'angular-ui-router',
    'app/core/core.services',
    'common/yangutils/yangutils.module'
], function() {

    var lispui = angular.module('app.lispui', ['ui.router.state',
        'app.core', 'app.common.yangUtils', 'restangular'
    ]);

    lispui.config(function($stateProvider, $controllerProvider,
        $compileProvider, $provide, $translateProvider, $urlRouterProvider,
        NavHelperProvider) {

        $urlRouterProvider.otherwise('/lispui/keys');

        $translateProvider.useStaticFilesLoader({
            prefix: '/src/app/lispui/assets/data/locale-',
            suffix: '.json'
        });

        lispui.register = {
            controller: $controllerProvider.register,
            directive: $compileProvider.directive,
            service: $provide.service,
            factory: $provide.factory
        };

        var access = routingConfig.accessLevels;
        $stateProvider.state('main.lispui', {
            url: 'lispui',
            abstract: true,
            views: {
                'content': {
                    templateUrl: '/src/app/lispui/root.tpl.html',
                    controller: 'RootLispuiCtrl'
                }
            }
        });

        $stateProvider.state('main.lispui.keys', {
            url: '/keys',
            views: {
                'lispui': {
                    templateUrl: '/src/app/lispui/keys/key.tpl.html',
                    controller: 'KeysLispuiCtrl'
                }
            }
        });

        $stateProvider.state('main.lispui.keys-create', {
            url: '/keys-create',
            views: {
                'lispui': {
                    templateUrl: '/src/app/lispui/keys/key.create.tpl.html',
                    controller: 'KeysCreateLispuiCtrl'
                }
            }
        });

        $stateProvider.state('main.lispui.keys-get', {
            url: '/keys-get',
            views: {
                'lispui': {
                    templateUrl: '/src/app/lispui/keys/key.get.tpl.html',
                    controller: 'KeysGetLispuiCtrl'
                }
            }
        });

        $stateProvider.state('main.lispui.mappings', {
            url: '/mappings',
            views: {
                'lispui': {
                    templateUrl: '/src/app/lispui/mappings/mapping.tpl.html',
                    controller: 'MappingsLispuiCtrl'
                }
            }
        });

        $stateProvider.state('main.lispui.mappings-create', {
            url: '/mappings-create',
            views: {
                'lispui': {
                    templateUrl: '/src/app/lispui/mappings/mapping.create.tpl.html',
                    controller: 'MappingsCreateLispuiCtrl'
                }
            }
        });

        $stateProvider.state('main.lispui.mappings-get', {
            url: '/mappings-get',
            views: {
                'lispui': {
                    templateUrl: '/src/app/lispui/mappings/mapping.get.tpl.html',
                    controller: 'MappingsGetLispuiCtrl'
                }
            }
        });

        NavHelperProvider.addToMenu('lispui', {
            "link": "#/lispui/keys",
            "title": "LISP UI",
            "active": "main.lispui",
            "icon": "icon-sitemap",
            "page": {
                "title": "LISP UI",
                "description": "LISP UI"
            }
        });

        NavHelperProvider.addControllerUrl(
            'app/lispui/lispui.controller');
        NavHelperProvider.addControllerUrl(
            'app/lispui/keys/lispui.key.controller');
        NavHelperProvider.addControllerUrl(
            'app/lispui/mappings/lispui.mapping.controller'
        );

    });

    return lispui;
});
