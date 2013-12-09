define 'viewmodels/MainViewModel',
    ["webjars!knockout.js", "webjars!sammy.js", "jquery", "models/Document", "models/User"],
    (ko, Sammy, $, Document, User) ->
        class MainViewModel
            constructor: () ->
                self = @
                @user = ko.observable()
                @getCurrentUser()

                @document = ko.observable()
                @documentList = ko.observableArray()
                @getDocuments()

                @newDocumentTitle = ko.observable()


                router = Sammy () ->
                    @get "/", () ->
                        self.selectFirstDocument()
                    @get "/#/:documentId", () ->
                        self.getDocument(@params.documentId)

                router.run()

                $('.chosen-select').chosen()

            updateUserInfo: () ->
                User.updateInfo(@user().firstName(), @user().lastName()).done (user) =>
                    @user(user)
                    $('#profile-edit').modal('hide')

            getCurrentUser: () ->
                User.getCurrentUser().done @user

            getDocuments: () ->
                Document.getList().done @documentList

            getDocument: (id) ->
                Document.get(id).done @document

            createDocument: () ->
                Document.create(@newDocumentTitle).done (document) =>
                    $("#create-form").modal('hide')
                    @newDocumentTitle('')
                    @documentList.unshift(document)
                    @selectDocument(document)

            deleteDocument: (context) ->
                Document.remove(@document()).done (data) =>
                    $("#delete-confirm").modal('hide')
                    @documentList.remove((item) => item.id() == @document().id())
                    @selectFirstDocument()

            selectDocument: (document) -> location.hash = "/" + document.id()

            selectFirstDocument: () ->
                if @documentList().length == 0
                    subscription = @documentList.subscribe (documents) =>
                        @selectDocument(documents[0])
                        subscription.dispose()
                else
                    @selectDocument(@documentList()[0])
