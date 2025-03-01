Page {
    padding: "8"

    Column {
        padding: "8"

        Markdown {
            
            text: "# About the author"
        }

        Spacer { amount: 16 }
        Image { src: "olaf.jpg" }
        Spacer { amount: 8 }
        Markdown {
												fontSize: 16
            text: "
                Olaf was born 1963 in Hamburg. 
                He studied graphics design and 
																human computer interaction design.
                "
        }
        Spacer { weight: 1 }
        Button { label: "Home" link: "page:home" }
    }
}