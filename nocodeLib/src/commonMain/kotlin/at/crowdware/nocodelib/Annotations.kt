package at.crowdware.nocodelib


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoreForDocumentation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class MarkdownAnnotation(val description: String = """
            Enter text using Markdown syntax to format the content.
            Supported elements include:
            
            - **Headings**: Use '#' for headings (e.g., `# Heading 1`, `## Heading 2`, `### Heading 3`).
            - **Bold Text**: Wrap text in double asterisks (e.g., `**bold text**`).
            - **Italic Text**: Wrap text in single asterisks (e.g., `*italic text*`).
            - **Lists**: 
              - Unordered lists: Use asterisks or dashes (e.g., `* Item 1`, `- Item 2`).
              - Ordered lists: Use numbers (e.g., `1. First item`).
            - **Links**: Format as `[link text](URL)` (e.g., `[Google](https://www.google.com)`).
            - **Images**: Format as `![alt text](image URL)` (e.g., `![My Image](https://example.com/image.png)`).
            - **Blockquotes**: Start a line with `>` to create a blockquote (e.g., `> This is a quote`).
            - **Code**: Use backticks for inline code (e.g., `` `code` ``) and triple backticks for code blocks:
              ```
              ```
              code block
              ```
              ```
              
            Example: 
            ```
            # Heading 1
            
            This is a paragraph with **bold text** and *italic text*.
            
            ## List
            - Item 1
            - Item 2
            
            [Link to Google](https://www.google.com)
            ```
        """)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class HexColorAnnotation(val description: String = """
            Enter a hex color value to specify the a color.
            The color should be in the format '#RRGGBB' or '#RGB':
            
            - **Full format**: '#FF5733' (where 'FF' is red, '57' is green, and '33' is blue).
            - **Short format**: '#F53' (where 'F5' is red, '3' is green and blue, equalizing their values).
            
            Ensure the hex string starts with '#' and contains valid hexadecimal characters (0-9, A-F).
            Example: For a vibrant orange color, use '#FF5733'.
        """)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PaddingAnnotation(val description: String = """
            Enter padding values in the format 'top right bottom left'.
            You can also provide one to four values:
            
            - **One value** (e.g., '16'): Applies the same padding to all sides.
            - **Two values** (e.g., '8 16'): Applies the first value to the top and bottom, and the second value to the left and right.
            - **Three values** (e.g., '8 16 32'): Applies the first value to the top, the second to the left and right, and the third to the bottom.
            - **Four values** (e.g., '8 16 32 48'): Applies values to the top, right, bottom, and left in that order.
            
            Example: For '8 16 32 48', padding will be set as:
            - Top: 8
            - Right: 16
            - Bottom: 32
            - Left: 48
        """)