package github.yaa110.memento.model

class Folder {
    var name: String
    var path: String
    var isBack: Boolean
    var isDirectory: Boolean

    constructor(name: String, path: String, isBack: Boolean) {
        this.name = name
        this.path = path
        this.isBack = isBack
        this.isDirectory = true
    }

    constructor(name: String, path: String, isBack: Boolean, isDirectory: Boolean) {
        this.name = name
        this.path = path
        this.isBack = isBack
        this.isDirectory = isDirectory
    }
}
