/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2014-11-17 18:43:33 UTC)
 * on 2014-12-06 at 21:07:59 UTC 
 * Modify at your own risk.
 */

package epusp.pcs.os.workflow.emcallworkflowendpoint.model;

/**
 * Model definition for DrivingLicence.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the emcallworkflowendpoint. For a detailed explanation
 * see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class DrivingLicence extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Agent agent;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String category;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean hasAcategory;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String licenceType;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String registerCode;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private com.google.api.client.util.DateTime validUntil;

  /**
   * @return value or {@code null} for none
   */
  public Agent getAgent() {
    return agent;
  }

  /**
   * @param agent agent or {@code null} for none
   */
  public DrivingLicence setAgent(Agent agent) {
    this.agent = agent;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getCategory() {
    return category;
  }

  /**
   * @param category category or {@code null} for none
   */
  public DrivingLicence setCategory(java.lang.String category) {
    this.category = category;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getHasAcategory() {
    return hasAcategory;
  }

  /**
   * @param hasAcategory hasAcategory or {@code null} for none
   */
  public DrivingLicence setHasAcategory(java.lang.Boolean hasAcategory) {
    this.hasAcategory = hasAcategory;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getLicenceType() {
    return licenceType;
  }

  /**
   * @param licenceType licenceType or {@code null} for none
   */
  public DrivingLicence setLicenceType(java.lang.String licenceType) {
    this.licenceType = licenceType;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getRegisterCode() {
    return registerCode;
  }

  /**
   * @param registerCode registerCode or {@code null} for none
   */
  public DrivingLicence setRegisterCode(java.lang.String registerCode) {
    this.registerCode = registerCode;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public com.google.api.client.util.DateTime getValidUntil() {
    return validUntil;
  }

  /**
   * @param validUntil validUntil or {@code null} for none
   */
  public DrivingLicence setValidUntil(com.google.api.client.util.DateTime validUntil) {
    this.validUntil = validUntil;
    return this;
  }

  @Override
  public DrivingLicence set(String fieldName, Object value) {
    return (DrivingLicence) super.set(fieldName, value);
  }

  @Override
  public DrivingLicence clone() {
    return (DrivingLicence) super.clone();
  }

}
