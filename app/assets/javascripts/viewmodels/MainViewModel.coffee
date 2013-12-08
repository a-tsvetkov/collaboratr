define 'viewmodels/MainViewModel',
    ["webjars!knockout.js", "webjars!sammy.js", "jquery", "models/Document", "models/User"],
    (ko, Sammy, $, Document, User) ->
        class MainViewModel
            constructor: () ->
                self = @
                @document = ko.observable()
                @documentList = ko.observableArray()
                @getDocuments()
                @newDocumentTitle = ko.observable()

                @user = ko.observable(new User({}))
                @getCurrentUser()

                router = Sammy () ->
                    @get "/", () ->
                        self.selectFirstDocument()
                    @get "/#/:documentId", () ->
                        self.getDocument(@params.documentId)

                router.run()

                $('.chosen-select').chosen()

            updateUserInfo: () ->
                User.updateInfo @user().firstName(), @user().lastName(), (data) =>
                    $('#profile-edit').modal('hide')
                    @user(new User(data))

            getCurrentUser: () ->
                User.getCurrentUser (data) =>
                    @user(new User(data))

            getDocuments: () ->
                Document.getList (data) =>
                    @documentList(new Document(item) for item in data.items)

            getDocument: (id) ->
                Document.get id, (data) =>
                    @document(new Document(data))

            createDocument: () ->
                Document.create @newDocumentTitle, (data) =>
                    $("#create-form").modal('hide')
                    @newDocumentTitle('')
                    document = new Document(data)
                    @documentList.unshift(document)
                    @selectDocument(document)

            deleteDocument: (context) ->
                Document.remove @document(), (data) =>
                    $("#delete-confirm").modal('hide')
                    @documentList.remove((item) => item.id() == @document().id())
                    @selectFirstDocument()

            selectDocument: (document) ->
                location.hash = "/" + document.id()

            selectFirstDocument: () ->
                if @documentList().length == 0
                    subscription = @documentList.subscribe (documents) =>
                        @selectDocument(documents[0])
                        subscription.dispose()
                else
                    @selectDocument(@documentList()[0])
