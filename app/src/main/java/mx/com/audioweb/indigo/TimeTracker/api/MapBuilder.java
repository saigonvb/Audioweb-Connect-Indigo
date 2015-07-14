package mx.com.audioweb.indigo.TimeTracker.api;

public class MapBuilder {

    // static strings used for writing file for displaying google map web view
    public static String map_content = "<!DOCTYPE html><html><head><title>Simple Map</title>"
            + "<meta name="
            + "\"viewport\""
            + " content="
            + "\"initial-scale=1.0, user-scalable=no\">"
            + "<meta charset="
            + "\"utf-8\""
            + "><style>html, body, #map-canvas {margin: 0;padding: 0;height: 100%;}</style> "
            + "<script src="
            + "\"https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false\" "
            + "></script><script>var map; function initialize() {";

    public static String map_content2 = "}google.maps.event.addDomListener(window, 'load', initialize);</script></head><body>"
            + "<div id=" + "\"map-canvas\"" + "></div></body></html>";

}
