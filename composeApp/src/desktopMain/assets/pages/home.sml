Page {
	padding: "8"

	Column {
		padding: "8"
						
		Markdown {
			text: "
				# Header 1
				## Header 2
				### Header 3
				#### Header 4
				##### Header 5
				(c) (tm) (r)
				"
		}
		Spacer { amount: 8 }
		Image { src: "ship.png" }
  Spacer { weight: 1 }
		Button { label: "About" link: "page:about"}
  Spacer { amount: 4 } 
		Button { label: "Videos" link: "page:video"}
	}
}