package com.unilol.comp4521.unilol.interfaces

data class Profile (
    var id: String = "",
    var email: String = "",
    var fullName: String = "",
    var profilePictureUrl: String = "",
    var username: String = "",
    var school: String = "HKUST",
    var status: String = "This guy is too lazy to write his status"
)