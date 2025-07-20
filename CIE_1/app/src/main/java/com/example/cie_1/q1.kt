package com.example.cie_1

class q1

{
    fun main(args : Array<String>)
    {
        class Rider()
        {
            var name: String = ""

            var destination: String = ""
            var rating : Int =0
            var method : String = ""

            var trip_history: MutableList<String> = mutableListOf()

            fun update_rider_Profile(name: String, destination: String, rating: Int, method: String)
            {
                this.name = name
                this.destination = destination
                this.rating = rating
                this.method = method
            }

            fun inputInfo()
            {
                println("Enter Rider Name: ")
                name = readLine() ?: ""


                println("Enter Destination: ")
                destination = readLine() ?: ""
                trip_history.add(destination)

                println("Enter Rating: ")
                rating = readLine()?.toIntOrNull() ?: 0

                println("Enter Payment Method: ")
                method = readLine() ?: ""


            }

            fun displayInfo()
            {
                println("Rider Name: $name")
                println("Destination: $destination")
                print("Payment Method:$method ")
                println("Rating Given: $rating")
                println("Trip History: $trip_history")


            }


        }

        class Driver()
        {
            var name: String = ""
            var age: Int = 0
            var licenseNumber: String = ""
            var rating : Int =0

            fun inputInfo()
            {
                println("Enter Driver Name: ")
                name = readLine() ?: ""

                println("Enter Driver Age: ")
                age = readLine()?.toIntOrNull() ?: 0

                println("Enter License Number: ")
                licenseNumber = readLine() ?: ""



            }
            fun update_driver_Profile(name: String, age: Int, licenseNumber: String)
            {
                this.name = name
                this.age = age
                this.licenseNumber = licenseNumber
            }

            fun displayInfo()
            {
                println("Driver Name: $name")
                println("Driver Age: $age")
                println("License Number: $licenseNumber")
                print("Rating: ")
            }

        }

       Rider ().apply {
            inputInfo()
            displayInfo()
        }

        Driver().apply {
            inputInfo()
            displayInfo()
        }








    }

}