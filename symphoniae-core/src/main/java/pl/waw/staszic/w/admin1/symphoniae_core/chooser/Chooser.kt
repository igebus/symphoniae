package pl.waw.staszic.w.admin1.symphoniae_core.chooser

interface Chooser {
    fun selectItem(index : Int)
    fun getSelected() : Int
    fun getPrevious() : Int
    fun selectPrevious()
    fun removeOption(index : Int)
}