package com.example.cie_1


class q2(args : Array<String>)

{
    var total=0;
    var users = 0;
    var split = total/users;


    fun main(args: Array<String>)
    {

        println("Enter the number of users: ")
        users = readLine()!!.toInt()

        if (users <= 0) {
            println("Number of users must be greater than zero.")
            return
        }

        println("ENTER NAMES OF PEOPLE TO SPLIT THE BILL: ")
        for (i in 1..users)
        {
            println("Enter name of user $i: ")
            val name = readLine()
            println("User $i: $name")
        }

        println("Enter the total amount: ")
        total = readLine()!!.toInt()

        split = total / users
        println("Each user should pay: $split")

        for (i in 1..users)
        {
            println("User $i pays: $split")
        }
    }

}


