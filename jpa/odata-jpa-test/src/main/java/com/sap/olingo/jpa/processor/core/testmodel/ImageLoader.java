package com.sap.olingo.jpa.processor.core.testmodel;

import static org.junit.jupiter.api.Assertions.fail;

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
  /**
   * 
   */
  private static final String SELECT_PERSON_IMAGE =
      "SELECT * FROM \"OLINGO\".\"PersonImage\" WHERE ID = '$&1'";
  private static final String SELECT_ORGANIZATION_IMAGE =
      "SELECT * FROM \"OLINGO\".\"OrganizationImage\" WHERE ID = '$&1'";
  private static final String PATH = "images/";
  private static final String ENTITY_MANAGER_DATA_SOURCE = "javax.persistence.nonJtaDataSource";
  private static final String PUNIT_NAME = "com.sap.olingo.jpa";

  public static void main(String[] args) throws IOException {

    final ImageLoader i = new ImageLoader();
    final EntityManager em = createEntityManager();
    i.loadPerson(em, "OlingoOrangeTM.png", "99");
  }

  public void loadPerson(EntityManager em, String imageName, String businessPartnerID) throws IOException {

    final byte[] image = loadImage(imageName);
    storePersonImageDB(em, image, businessPartnerID, SELECT_PERSON_IMAGE);
  }

  public void loadPerson(String imageName, String businessPartnerID) throws IOException {

    final byte[] image = loadImage(imageName);
    storePersonImageDB(createEntityManager(), image, businessPartnerID, SELECT_PERSON_IMAGE);
  }

  public void loadOrg(EntityManager em, String imageName, String businessPartnerID) throws IOException {

    final byte[] image = loadImage(imageName);
    storeOrgImageDB(em, image, businessPartnerID, SELECT_ORGANIZATION_IMAGE);
  }

  public void loadOrg(String imageName, String businessPartnerID) throws IOException {

    final byte[] image = loadImage(imageName);
    storeOrgImageDB(createEntityManager(), image, businessPartnerID, SELECT_ORGANIZATION_IMAGE);
  }

  private void storePersonImageDB(EntityManager em, byte[] image, String businessPartnerID, String query) {

    final String s = query.replace("$&1", businessPartnerID);
    final Query q = em.createNativeQuery(s, PersonImage.class);
    @SuppressWarnings("unchecked")
    final List<PersonImage> result = q.getResultList();
    result.get(0).setImage(image);
    updateDB(em, result);

    final Query storedImageQ = em.createNativeQuery(s, PersonImage.class);
    @SuppressWarnings("unchecked")
    final List<PersonImage> result2 = storedImageQ.getResultList();
    final byte[] storedImage = result2.get(0).getImage();
    compareImage(image, storedImage);
  }

  private void storeOrgImageDB(EntityManager em, byte[] image, String businessPartnerID, String query) {

    final String s = query.replace("$&1", businessPartnerID);
    final Query q = em.createNativeQuery(s, OrganizationImage.class);
    @SuppressWarnings("unchecked")
    final List<OrganizationImage> result = q.getResultList();
    result.get(0).setImage(image);
    updateDB(em, result);

    final Query storedImageQ = em.createNativeQuery(s, OrganizationImage.class);
    @SuppressWarnings("unchecked")
    final List<OrganizationImage> result2 = storedImageQ.getResultList();
    final byte[] storedImage = result2.get(0).getImage();
    compareImage(image, storedImage);
  }

  private void updateDB(EntityManager em, List<?> result) {
    em.getTransaction().begin();
    em.persist(result.get(0));
    em.getTransaction().commit();
  }

  private static EntityManager createEntityManager() {
    final Map<String, Object> properties = new HashMap<>();
    properties.put(ENTITY_MANAGER_DATA_SOURCE, DataSourceHelper.createDataSource(DataSourceHelper.DB_H2));
    final EntityManagerFactory emf = Persistence.createEntityManagerFactory(PUNIT_NAME, properties);
    return emf.createEntityManager();
  }

  private void compareImage(byte[] image, byte[] storedImage) {
    if (image.length != storedImage.length)
      fail("[Image]: length miss match");
    else {
      for (int i = 0; i < image.length; i++) {
        if (image[i] != storedImage[i]) {
          fail("[Image]: mismatch at" + Integer.toString(i));
          break;
        }
      }
    }
  }

  public void storeImageLocal(final byte[] storedImage, final String fileName) throws IOException {

    final String home = System.getProperty("user.home");
    final String filePath = home + "\\Downloads\\" + fileName;

    try (final OutputStream o = new FileOutputStream(filePath)) {
      o.write(storedImage);
      o.flush();
    }
  }

  private byte[] loadImage(final String imageName) throws IOException {

    final String path = PATH + imageName;
    byte[] image = null;
    final URL u = this.getClass().getClassLoader().getResource(path);
    try (final InputStream i = u.openStream()) {
      image = new byte[i.available()];
      final int noBytes = i.read(image);
      if (noBytes == -1)
        return new byte[] {};
      return image;
    }
  }
}
