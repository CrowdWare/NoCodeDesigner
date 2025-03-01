Page {
  backgroundColor: "#000000"
  color: "#ffffff"
  padding: "16"
  scrollable: "true"

  Markdown { text: "# Videos" }
  Markdown { text: "Here are some sample videos." }
  Spacer { amount: 16 }
  Markdown { text: "## Local Video"}
  Spacer { amount: 6 }
    
  Video { src: "https://crowdware.github.io/NoCodeBrowser/assets/beach.mp4"}
    
  Spacer { amount: 16 }
  Markdown { text: "## YouTube"}
  Spacer { amount: 8 }
  Youtube { id: "gG1jxR8fkDY" }
  Spacer { amount: 8 }
  Image { src: "ship.png" }
  Spacer { amount: 8 }
  Image { src: "ship.png" }
    
  
  Button { label: "Home" link: "page:home" }
}