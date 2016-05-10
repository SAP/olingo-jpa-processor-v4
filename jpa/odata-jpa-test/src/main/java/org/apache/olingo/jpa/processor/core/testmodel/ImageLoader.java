package org.apache.olingo.jpa.processor.core.testmodel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class ImageLoader {
  private static final String SELECT_PERSON_IMAGE =
      "SELECT * FROM \"OLINGO\".\"org.apache.olingo.jpa::PersonImage\" WHERE ID = '$&1'";
  private static final String SELECT_ORGANIZATION_IMAGE =
      "SELECT * FROM \"OLINGO\".\"org.apache.olingo.jpa::OrganizationImage\" WHERE ID = '$&1'";
  private static final String PATH = "images/";
  private static final String TEST_IMAGE = "test.png";
  private static final String ENTITY_MANAGER_DATA_SOURCE = "javax.persistence.nonJtaDataSource";
  private static final String PUNIT_NAME = "org.apache.olingo.jpa";

  public static void main(String[] args) throws Exception {
    ImageLoader i = new ImageLoader();
    i.loadPerson("OlingoOrangeTM.png", "99");

  }

  public void loadPerson(String imageName, String businessPartnerID) {
    byte[] image = loadImage(imageName);
    storePersonImageDB(image, businessPartnerID, SELECT_PERSON_IMAGE);
    storeImageLocal(image, "restored.png");
  }

  public void loadOrg(String imageName, String businessPartnerID) {
    byte[] image = loadImage(imageName);
    storeOrgImageDB(image, businessPartnerID, SELECT_ORGANIZATION_IMAGE);
    storeImageLocal(image, "restored.png");
  }

  private void storePersonImageDB(byte[] image, String businessPartnerID, String query) {

    EntityManager em = createEntityManager();

    String s = query.replace("$&1", businessPartnerID);
    Query q = em.createNativeQuery(s, PersonImage.class);
    @SuppressWarnings("unchecked")
    List<PersonImage> result = q.getResultList();
    result.get(0).setImage(image);
    updateDB(em, result);

    Query storedImageQ = em.createNativeQuery(s, PersonImage.class);
    @SuppressWarnings("unchecked")
    List<PersonImage> result2 = storedImageQ.getResultList();
    byte[] storedImage = result2.get(0).getImage();
    System.out.println(storedImage.length);
    compareImage(image, storedImage);
    storeImageLocal(storedImage, TEST_IMAGE);

  }

  private void storeOrgImageDB(byte[] image, String businessPartnerID, String query) {

    EntityManager em = createEntityManager();

    String s = query.replace("$&1", businessPartnerID);
    Query q = em.createNativeQuery(s, OrganizationImage.class);
    @SuppressWarnings("unchecked")
    List<OrganizationImage> result = q.getResultList();
    result.get(0).setImage(image);
    updateDB(em, result);

    Query storedImageQ = em.createNativeQuery(s, OrganizationImage.class);
    @SuppressWarnings("unchecked")
    List<OrganizationImage> result2 = storedImageQ.getResultList();
    byte[] storedImage = result2.get(0).getImage();
    System.out.println(storedImage.length);
    compareImage(image, storedImage);
    storeImageLocal(storedImage, TEST_IMAGE);

  }

  private void updateDB(EntityManager em, List<?> result) {
    em.getTransaction().begin();
    em.persist(result.get(0));
    em.getTransaction().commit();
  }

  private EntityManager createEntityManager() {
    final Map<String, Object> properties = new HashMap<String, Object>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, DataSourceHelper.createDataSource(DataSourceHelper.DB_H2));
    final EntityManagerFactory emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
    EntityManager em = emf.createEntityManager();
    return em;
  }

  private void compareImage(byte[] image, byte[] storedImage) {
    if (image.length != storedImage.length)
      System.out.println("[Image]: length miss match");
    else {
      for (int i = 0; i < image.length; i++) {
        if (image[i] != storedImage[i]) {
          System.out.println("[Image]: missmatch at" + Integer.toString(i));
          break;
        }
      }
    }
  }

  public void storeImageLocal(byte[] storedImage, String fileName) {

    String home = System.getProperty("user.home");
    String filePath = home + "\\" + "Downloads" + "\\" + fileName;

    OutputStream o = null;
    try {
      o = new FileOutputStream(filePath);
      o.write(storedImage);
      o.flush();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        o.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  private byte[] loadImage(String imageName) {
    String path = PATH + imageName;
    InputStream i = null;
    byte[] image = null;
    URL u = this.getClass().getClassLoader().getResource(path);
    try {
      i = u.openStream();
      image = new byte[i.available()];
      i.read(image);
    } catch (IOException e1) {
      e1.printStackTrace();
    } finally {
      try {
        i.close();
        return image;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
}
