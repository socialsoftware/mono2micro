package pt.ist.socialsoftware.mono2micro.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pt.ist.socialsoftware.mono2micro.cluster.Cluster;
import pt.ist.socialsoftware.mono2micro.decomposition.domain.Decomposition;
import pt.ist.socialsoftware.mono2micro.element.Element;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportUtils {

    public static void addDataToZipFile(String entryName, byte[] data, ZipOutputStream zipOutputStream) throws IOException {
        zipOutputStream.putNextEntry(new ZipEntry(entryName));
        zipOutputStream.write(data);
        zipOutputStream.closeEntry();
    }

    public static void addDataToZipFile(String entryName, Decomposition decomposition, ZipOutputStream zipOutputStream) throws IOException, JSONException {
        addDataToZipFile(entryName, serializeDecomposition(decomposition).getBytes(), zipOutputStream);
    }

    private static String serializeDecomposition(Decomposition decomposition) throws JSONException {

        JSONObject data = new JSONObject();
        JSONObject decompositionObject = new JSONObject();
        JSONArray clustersList = new JSONArray();

        for (Cluster cluster : decomposition.getClusters().values()) {
            JSONObject clusterObject = new JSONObject();
            clusterObject.put("name", cluster.getName());

            JSONArray elementsList = new JSONArray();
            for (Element element : cluster.getElements()) {
                JSONObject elementObject = new JSONObject();
                elementObject.put("id", element.getId());
                elementObject.put("name", element.getName());
                elementsList.put(elementObject);
            }

            clusterObject.put("elements", elementsList);
            clustersList.put(clusterObject);
        }

        decompositionObject.put("clusters", clustersList);
        data.put("decomposition", decompositionObject);
        return data.toString(1);
    }
}
