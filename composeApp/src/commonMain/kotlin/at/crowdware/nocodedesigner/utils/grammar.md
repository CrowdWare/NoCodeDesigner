# SML GRAMMAR for AI requests for SML generation


// Tokens
TOKEN identifier: "[a-zA-Z_][a-zA-Z0-9_]*"
TOKEN lBrace: "{"
TOKEN rBrace: "}"
TOKEN colon: ":"
TOKEN stringLiteral: "\"[^\"]*\""
TOKEN whitespace: "\\s+"
TOKEN integerLiteral: "\\d+"
TOKEN floatLiteral: "\\d+\\.\\d+"
TOKEN lineComment: "//.*"
TOKEN blockComment: "/\\*[\\s\\S]*?\\*/"

// Grammar Rules
Grammar SmlGrammar {

    // Whitespace and comments are ignored
    ignored: whitespace | lineComment | blockComment

    // Property Value Types
    stringValue: stringLiteral -> PropertyValue.StringValue(value)
    intValue: integerLiteral -> PropertyValue.IntValue(value.toInt())
    floatValue: floatLiteral -> PropertyValue.FloatValue(value.toFloat())

    // Define Property Value
    propertyValue: floatValue | intValue | stringValue

    // Define a Property
    property: ignored* identifier ignored* colon ignored* propertyValue -> (id, value)

    // Element Content can contain properties or nested elements
    elementContent: (property | element)*

    // Define an Element
    element: ignored* identifier ignored* lBrace elementContent ignored* rBrace

    // Root Parser for the entire structure
    root: (element+ ignored*) -> elements
}