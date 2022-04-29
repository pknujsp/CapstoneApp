package com.lifedawn.capstoneapp.retrofits.response.naver.Directions;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DirectionsResponse {
   @SerializedName("code")
   @Expose
   private String code;

   @SerializedName("message")
   @Expose
   private String message;

   @SerializedName("currentDateTime")
   @Expose
   private String currentDateTime;

   @SerializedName("route")
   @Expose
   private Route route;

   public String getCode() {
      return code;
   }

   public void setCode(String code) {
      this.code = code;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getCurrentDateTime() {
      return currentDateTime;
   }

   public void setCurrentDateTime(String currentDateTime) {
      this.currentDateTime = currentDateTime;
   }

   public Route getRoute() {
      return route;
   }

   public void setRoute(Route route) {
      this.route = route;
   }

   public static class Route{
      @SerializedName("trafast")
      @Expose
      private List<Trafast> trafast;

      public List<Trafast> getTrafast() {
         return trafast;
      }

      public void setTrafast(List<Trafast> trafast) {
         this.trafast = trafast;
      }

      public static class Trafast{
         @SerializedName("summary")
         @Expose
         private Summary summary;

         public Summary getSummary() {
            return summary;
         }

         public void setSummary(Summary summary) {
            this.summary = summary;
         }

         public static class Summary{
            @SerializedName("distance")
            @Expose
            private String distance;

            @SerializedName("duration")
            @Expose
            private String duration;

            @SerializedName("taxiFare")
            @Expose
            private String taxiFare;

            @SerializedName("tollFare")
            @Expose
            private String tollFare;

            @SerializedName("fuelPrice")
            @Expose
            private String fuelPrice;

            @SerializedName("start")
            @Expose
            private List<String> start;

            @SerializedName("goal")
            @Expose
            private List<String> goal;

            public String getDistance() {
               return distance;
            }

            public void setDistance(String distance) {
               this.distance = distance;
            }

            public String getDuration() {
               return duration;
            }

            public void setDuration(String duration) {
               this.duration = duration;
            }

            public String getTaxiFare() {
               return taxiFare;
            }

            public void setTaxiFare(String taxiFare) {
               this.taxiFare = taxiFare;
            }

            public String getTollFare() {
               return tollFare;
            }

            public void setTollFare(String tollFare) {
               this.tollFare = tollFare;
            }

            public String getFuelPrice() {
               return fuelPrice;
            }

            public void setFuelPrice(String fuelPrice) {
               this.fuelPrice = fuelPrice;
            }

            public List<String> getStart() {
               return start;
            }

            public void setStart(List<String> start) {
               this.start = start;
            }

            public List<String> getGoal() {
               return goal;
            }

            public void setGoal(List<String> goal) {
               this.goal = goal;
            }
         }
      }
   }
}
