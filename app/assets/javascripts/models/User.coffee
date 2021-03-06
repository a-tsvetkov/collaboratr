define 'models/User',
    ['webjars!knockout.js', 'jquery', 'jsRoutes']
    (ko, $, jsRoutes) ->
        class User
            constructor: (data) ->
                @id = ko.observable(data.id)
                @email = ko.observable(data.email)
                @firstName = ko.observable(data.firstName)
                @lastName = ko.observable(data.lastName)
                @dateJoined = ko.observable(data.dateJoined)

                @fullName = ko.computed () =>
                    result = ''
                    if @firstName()
                        result += @firstName()

                    if @lastName()
                        result += " #{@lastName()}"

                    result.trim()

                @displayName = ko.computed () =>
                    if @fullName() then "#{@fullName()} (#{@email()})" else @email()

            @getCurrentUser: () ->
                $.ajax(jsRoutes.controllers.UserApi.me()).then((data) -> new User(data)).promise()

            @updateInfo: (firstName, lastName) ->
                r = jsRoutes.controllers.UserApi.updateInfo()
                $.ajax(
                    url: r.url
                    type: r.type
                    contentType: 'application/json'
                    dataType: 'json'
                    data: JSON.stringify
                        firstName: firstName
                        lastName: lastName
                    ).then((data) -> new User(data))
                    .promise()
