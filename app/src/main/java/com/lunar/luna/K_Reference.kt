package com.lunar.luna

class K_Reference {
    //variables
    fun main(args: Array<String>){
        var age = 15
        println(age)
        flow()
        myFunction()
    }

    //Control flows (conditions)
    private fun flow() {
        var age = 17
        if(age>=17)
            println("You may vote now")
    }

    //functions
    fun myFunction(){
        println("my function was called")
    }

    //collections = array, dictionaries?





}