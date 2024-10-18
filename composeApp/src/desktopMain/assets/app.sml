App {
  name: "MyApp"
  icon: "icon.png"
  id: "at.crowdware.mynewid"
  smlVersion: "1.1"

  Navigation {
    type: "HorizontalPager"

    Item { page: "page:home" }
    Item { page: "page:about" }
    Item { page: "page:video" }
  }

 Theme {
    primary: "#FFB951"
    onPrimary: "#452B00"
    primaryContainer: "#633F00"
    onPrimaryContainer: "#FFDDB3"
    secondary: "#DDC2A1"
    onSecondary: "#3E2D16"
    secondaryContainer: "#56442A"
    onSecondaryContainer: "#FBDEBC"
    tertiary: "#B8CEA1"
    onTertiary: "#243515"
    tertiaryContainer: "#3A4C2A"
    onTertiaryContainer: "#D4EABB"
    error: "#FFB4AB"
    errorContainer: "#93000A"
    onError: "#690005"
    onErrorContainer: "#FFDAD6"
    background: "#1F1B16"
    onBackground: "#EAE1D9"
    surface: "#1F1B16"
    onSurface: "#EAE1D9"
    surfaceVariant: "#4F4539"
    onSurfaceVariant: "#D3C4B4"
    outline: "#9C8F80"
    inverseOnSurface: "#1F1B16"
    inverseSurface: "#EAE1D9"
    inversePrimary: "#825500"
    surfaceTint: "#FFB951"
    outlineVariant: "#4F4539"
    scrim: "#000000"
  }

// deployment start - don't edit here
Deployment {
  File { path: "about.sml" time: "2024.10.15 16.01.19" }
  File { path: "test.sml" time: "2024.10.11 17.57.50" }
  File { path: "video.sml" time: "2024.10.17 06.28.53" }
  File { path: "home.sml" time: "2024.10.15 16.02.12" }
  File { path: "ship.png" time: "2024.09.03 09.08.18" }
  File { path: "2024-06-25 11.40.17.jpg" time: "2024.10.11 14.38.19" }
  File { path: "olaf_small.jpg" time: "2024.08.08 04.54.59" }
  File { path: "art_anyona.jpg" time: "2024.10.11 05.25.11" }
  File { path: "olaf.jpg" time: "2023.06.11 04.47.15" }
}
// deployment end
}