package com.lifedawn.capstoneapp.model.firestore;

import android.os.Parcelable;

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

import com.lifedawn.capstoneapp.common.constants.Constant;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.KakaoLocalDocument;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.address.AddressResponse;
import com.lifedawn.capstoneapp.retrofits.response.kakaolocal.place.PlaceResponse;

import kotlinx.parcelize.Parcelize;

@Parcelize
data class PlaceDto(
        @get:Exclude
        var id: String,

        @get:Exclude
        var eventId: String?,

        @PropertyName("latitude")
        var latitude: String,

        @PropertyName("longitude")
        var longitude: String,

        @PropertyName("address")
        var address: String,

        @PropertyName("name")
        var name: String?,

        @PropertyName("placeId")
        var placeId: String?,

        @PropertyName("locationType")
        var locationType: String,
) : Parcelable {
    constructor() : this("", "", "", "", "", "", "", "")

    companion object {
        fun toLocationDto(document: KakaoLocalDocument): PlaceDto {
            val location = PlaceDto()

            // 주소인지 장소인지를 구분한다.
            if (document is PlaceResponse.Documents) {
                val placeDocuments = document as PlaceResponse.Documents

                location.placeId = placeDocuments.id
                location.name = placeDocuments.placeName
                location.address = placeDocuments.addressName
                location.latitude = placeDocuments.y
                location.longitude = placeDocuments.x
                location.locationType = Constant.PLACE.name
            } else if (document is AddressResponse.Documents) {
                val addressDocuments = document as AddressResponse.Documents

                location.address = addressDocuments.addressName
                location.latitude = addressDocuments.y
                location.longitude = addressDocuments.x
                location.locationType = Constant.ADDRESS.name
            }

            return location;
        }
    }

}
