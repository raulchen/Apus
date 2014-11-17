val weather =
  <rss>
    <channel>
      <title>Yahoo! Weather - Boulder, CO</title>
      <item>
        <title>Conditions for Boulder, CO at 2:54 pm MST</title>
        <forecast day="Thu" date="10 Nov 2011" low="37" high="58" text="Partly Cloudy"
                  code="29" />
      </item>
    </channel>
  </rss>

val forecast = (weather \ "channel" \ "item" \ "forecast" ).text


val foo = scala.xml.XML.loadString("<stream>")
