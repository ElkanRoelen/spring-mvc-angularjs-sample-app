angular.module('minutesCounterApp', ['editableTableWidgets', 'frontendServices', 'spring-security-csrf-token-interceptor'])
    .filter('excludeDeleted', function () {
        return function (input) {
            return _.filter(input, function (item) {
                return item.deleted == undefined || !item.deleted;
            });
        }
    })
    .controller('MinutesTrackerCtrl', ['$scope' , 'WorkService', 'UserService', '$timeout',
        function ($scope, WorkService, UserService, $timeout) {

            $scope.vm = {
                maxMinutesPerDay: 2000,
                currentPage: 1,
                totalPages: 0,
                originalWorks: [],
                works: [],
                isSelectionEmpty: true,
                errorMessages: [],
                infoMessages: []
            };

            updateUserInfo();
            loadWorkData(null, null, null, null, 1);


            function showErrorMessage(errorMessage) {
                clearMessages();
                $scope.vm.errorMessages.push({description: errorMessage});
            }

            function updateUserInfo() {
                UserService.getUserInfo()
                    .then(function (userInfo) {
                        $scope.vm.userName = userInfo.userName;
                        $scope.vm.maxMinutesPerDay = userInfo.maxMinutesPerDay;
                        $scope.vm.todaysMinutes = userInfo.todaysMinutes ? userInfo.todaysMinutes : 'None';
                        updateMinutesCounterStatus();
                    },
                    function (errorMessage) {
                        showErrorMessage(errorMessage);
                    });
            }

            function markAppAsInitialized() {
                if ($scope.vm.appReady == undefined) {
                    $scope.vm.appReady = true;
                }
            }

            function loadWorkData(fromDate, fromTime, toDate, toTime, pageNumber) {
                WorkService.searchWorks(fromDate, fromTime, toDate, toTime, pageNumber)
                    .then(function (data) {

                        $scope.vm.errorMessages = [];
                        $scope.vm.currentPage = data.currentPage;
                        $scope.vm.totalPages = data.totalPages;

                        $scope.vm.originalWorks = _.map(data.works, function (work) {
                            work.datetime = work.date + ' ' + work.time;
                            return work;
                        });

                        $scope.vm.works = _.cloneDeep($scope.vm.originalWorks);

                        _.each($scope.vm.works, function (work) {
                            work.selected = false;
                        });

                        markAppAsInitialized();

                        if ($scope.vm.works && $scope.vm.works.length == 0) {
                            showInfoMessage("No results found.");
                        }
                    },
                    function (errorMessage) {
                        showErrorMessage(errorMessage);
                        markAppAsInitialized();
                    });
            }

            function clearMessages() {
                $scope.vm.errorMessages = [];
                $scope.vm.infoMessages = [];
            }

            function updateMinutesCounterStatus() {
                var isMinutesOK = $scope.vm.todaysMinutes == 'None' || ($scope.vm.todaysMinutes <= $scope.vm.maxMinutesPerDay);
                $scope.vm.minutesStatusStyle = isMinutesOK ? 'cal-limit-ok' : 'cal-limit-nok';
            }

            function showInfoMessage(infoMessage) {
                $scope.vm.infoMessages = [];
                $scope.vm.infoMessages.push({description: infoMessage});
                $timeout(function () {
                    $scope.vm.infoMessages = [];
                }, 1000);
            }

            $scope.updateMaxMinutesPerDay = function () {
                $timeout(function () {

                    if ($scope.vm.maxMinutesPerDay < 0) {
                        return;
                    }

                    UserService.updateMaxMinutesPerDay($scope.vm.maxMinutesPerDay)
                        .then(function () {
                        },
                        function (errorMessage) {
                            showErrorMessage(errorMessage);
                        });
                    updateMinutesCounterStatus();
                });
            };

            $scope.selectionChanged = function () {
                $scope.vm.isSelectionEmpty = !_.any($scope.vm.works, function (work) {
                    return work.selected && !work.deleted;
                });
            };

            $scope.pages = function () {
                return _.range(1, $scope.vm.totalPages + 1);
            };

            $scope.search = function (page) {

                var fromDate = new Date($scope.vm.fromDate);
                var toDate = new Date($scope.vm.toDate);

                console.log('search from ' + $scope.vm.fromDate + ' ' + $scope.vm.fromTime + ' to ' + $scope.vm.toDate + ' ' + $scope.vm.toTime);

                var errorsFound = false;

                if ($scope.vm.fromDate && !$scope.vm.toDate || !$scope.vm.fromDate && $scope.vm.toDate) {
                    showErrorMessage("Both from and to dates are needed");
                    errorsFound = true;
                    return;
                }

                if (fromDate > toDate) {
                    showErrorMessage("From date cannot be larger than to date");
                    errorsFound = true;
                }

                if (fromDate.getTime() == toDate.getTime() && $scope.vm.fromTime &&
                    $scope.vm.toTime && $scope.vm.fromTime > $scope.vm.toTime) {
                    showErrorMessage("Inside same day, from time cannot be larger than to time");
                    errorsFound = true;
                }

                if (!errorsFound) {
                    loadWorkData($scope.vm.fromDate, $scope.vm.fromTime, $scope.vm.toDate, $scope.vm.toTime, page == undefined ? 1 : page);
                }

            };

            $scope.previous = function () {
                if ($scope.vm.currentPage > 1) {
                    $scope.vm.currentPage-= 1;
                    loadWorkData($scope.vm.fromDate, $scope.vm.fromTime,
                        $scope.vm.toDate, $scope.vm.toTime, $scope.vm.currentPage);
                }
            };

            $scope.next = function () {
                if ($scope.vm.currentPage < $scope.vm.totalPages) {
                    $scope.vm.currentPage += 1;
                    loadWorkData($scope.vm.fromDate, $scope.vm.fromTime,
                        $scope.vm.toDate, $scope.vm.toTime, $scope.vm.currentPage);
                }
            };

            $scope.goToPage = function (pageNumber) {
                if (pageNumber > 0 && pageNumber <= $scope.vm.totalPages) {
                    $scope.vm.currentPage = pageNumber;
                    loadWorkData($scope.vm.fromDate, $scope.vm.fromTime, $scope.vm.toDate, $scope.vm.toTime, pageNumber);
                }
            };

            $scope.add = function () {
                $scope.vm.works.unshift({
                    id: null,
                    datetime: null,
                    description: null,
                    minutes: null,
                    selected: false,
                    new: true
                });
            };

            $scope.delete = function () {
                var deletedWorkIds = _.chain($scope.vm.works)
                    .filter(function (work) {
                        return work.selected && !work.new;
                    })
                    .map(function (work) {
                        return work.id;
                    })
                    .value();

                WorkService.deleteWorks(deletedWorkIds)
                    .then(function () {
                        clearMessages();
                        showInfoMessage("deletion successful.");

                        _.remove($scope.vm.works, function(work) {
                            return work.selected;
                        });

                        $scope.selectionChanged();
                        updateUserInfo();

                    },
                    function () {
                        clearMessages();
                        $scope.vm.errorMessages.push({description: "deletion failed."});
                    });
            };

            $scope.reset = function () {
                $scope.vm.works = $scope.vm.originalWorks;
            };

            function getNotNew(works) {
                return  _.chain(works)
                    .filter(function (work) {
                        return !work.new;
                    })
                    .value();
            }

            function prepareWorksDto(works) {
                return  _.chain(works)
                    .each(function (work) {
                        if (work.datetime) {
                            var dt = work.datetime.split(" ");
                            work.date = dt[0];
                            work.time = dt[1];
                        }
                    })
                    .map(function (work) {
                        return {
                            id: work.id,
                            date: work.date,
                            time: work.time,
                            description: work.description,
                            minutes: work.minutes,
                            version: work.version
                        }
                    })
                    .value();
            }

            $scope.save = function () {

                var maybeDirty = prepareWorksDto(getNotNew($scope.vm.works));

                var original = prepareWorksDto(getNotNew($scope.vm.originalWorks));

                var dirty = _.filter(maybeDirty).filter(function (work) {

                    var originalWork = _.filter(original, function (orig) {
                        return orig.id === work.id;
                    });

                    if (originalWork.length == 1) {
                        originalWork = originalWork[0];
                    }

                    return originalWork && ( originalWork.date != work.date ||
                        originalWork.time != work.time || originalWork.description != work.description ||
                        originalWork.minutes != work.minutes)
                });

                var newItems = _.filter($scope.vm.works, function (work) {
                    return work.new;
                });

                var saveAll = prepareWorksDto(newItems);
                saveAll = saveAll.concat(dirty);

                $scope.vm.errorMessages = [];

                // save all new items plus the ones that where modified
                WorkService.saveWorks(saveAll).then(function () {
                        $scope.search($scope.vm.currentPage);
                        showInfoMessage("Changes saved successfully");
                        updateUserInfo();
                    },
                    function (errorMessage) {
                        showErrorMessage(errorMessage);
                    });

            };

            $scope.logout = function () {
                UserService.logout();
            }


        }]);

