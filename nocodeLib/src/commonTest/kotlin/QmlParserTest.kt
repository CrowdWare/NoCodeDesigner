import at.crowdware.nocodelib.parseQML
import kotlin.test.Test
import kotlin.test.assertEquals

class QmlParserTest {

    @Test
    fun testAddition() {
        val sum = 1 + 1
        assertEquals(2, sum, "Addition test failed")
    }

    @Test
    fun testQmlParser() {
        val qml = """
            Page {
                Text {
                    content: "test"
                    }
            }
            """
        val node = parseQML(qml)
        println("node: $node")
        assertEquals("QMLNode(name=Page, properties=[], children=[QMLNode(name=Text, properties=[(content, test)], children=[])])" ,
            node.toString(), "QML Parsing failed")
    }

    @Test
    fun testQmlParser_2() {
        val qml = """
            Page {
                Text { content: "test" }
            }
            """
        val node = parseQML(qml)
        println("node: $node")
        assertEquals("QMLNode(name=Page, properties=[], children=[QMLNode(name=Text, properties=[(content, test)], children=[])])" ,
            node.toString(), "QML Parsing failed")
    }

    @Test
    fun testQmlParser_3() {
        val qml = """
            Page { 
				Column {
				    Text { content: "Zeile 1" 
					    color:"#ffffff"
				    }
				}
            }
            """
        val node = parseQML(qml)
        println("node: $node")
        assertEquals("QMLNode(name=Page, properties=[], children=[QMLNode(name=Column, properties=[], children=[QMLNode(name=Text, properties=[(content, Zeile 1), (color, #ffffff)], children=[])])])" ,
            node.toString(), "QML Parsing failed")
    }

    @Test
    fun testQmlParser_4() {
        val qml = """
            Page { 
				Column {
				    Button { label: "About" link: "page:about" }
				}
            }
            """
        val node = parseQML(qml)
        println("node: $node")
        assertEquals("QMLNode(name=Page, properties=[], children=[QMLNode(name=Column, properties=[], children=[QMLNode(name=Button, properties=[(label, About), (link, page:about)], children=[])])])" ,
            node.toString(), "QML Parsing failed")
    }

    @Test
    fun testQmlParser_5() {
        val qml = """
            Page { 
				Column {
				    Button { label: "About" link: "page:about" 
}
				}
            }
            """
        val node = parseQML(qml)
        println("node: $node")
        assertEquals("QMLNode(name=Page, properties=[], children=[QMLNode(name=Column, properties=[], children=[QMLNode(name=Button, properties=[(label, About), (link, page:about)], children=[])])])" ,
            node.toString(), "QML Parsing failed")
    }

    @Test
    fun testQmlParser_6() {
        val qml = """
            Page { 
				Column {
					Button { link: "page:about" label: "About" }
					Button { label: "test" link: "page:home" }
				}
            }
            """
        val node = parseQML(qml)
        println("node: $node")
        assertEquals("" ,
            node.toString(), "QML Parsing failed")
    }
}