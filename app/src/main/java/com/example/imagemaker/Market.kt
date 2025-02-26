package com.example.imagemaker

data class Market(
    val id:Int=-1,
    val name_market:String="",
    val longitude:Double = 0.0,
    val latitude:Double = 0.0,
    val mailTo:String="empty"
)
data class Coordinate(
    var longitude:Double = 0.0,
    val latitude:Double = 0.0
)