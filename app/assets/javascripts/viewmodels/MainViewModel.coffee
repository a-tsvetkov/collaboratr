define 'viewmodels/MainViewModel',
    ["webjars!knockout.js", "webjars!sammy.js", "models/Document"],
    (ko, Sammy, Document) ->
        class MainViewModel
            constructor: () ->
                self = @
                @document = ko.observable()
                @documentList = ko.observableArray()

                @getDocuments()

                router = Sammy () ->
                    @get "/", () ->
                        self.selectFirstDocument()
                    @get "/#/:documentId", () ->
                        self.getDocument(@params.documentId)

                router.run()

            getDocuments: () ->
                Document.getList (data) =>
                    @documentList(new Document(item) for item in data.items)

            getDocument: (id) ->
                Document.get id, (data) =>
                    @document(new Document(data))

            deleteDocument: (document) ->
                document.delete (data) =>
                    @documentList.remove(document)
                    @selectFirstDocument()

            selectDocument: (document) ->
                location.hash = "/" + document.id()

            selectFirstDocument: () ->
                if @documentList().length == 0
                    subscription = @documentList.subscribe (documents) =>
                        @selectDocument(documents[0])
                        subscription.dispose()
                else
                    @selectDocument(documents[0])
