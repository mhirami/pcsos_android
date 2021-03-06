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
 * on 2014-12-14 at 11:57:32 UTC 
 * Modify at your own risk.
 */

package epusp.pcs.os.workflow.emcallworkflowendpoint.model;

/**
 * Model definition for Victim.
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
public final class Victim extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean active;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<java.lang.String> allAttributeKeys;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<IAttribute> allAttributes;

  static {
    // hack to force ProGuard to consider IAttribute used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(IAttribute.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<BooleanArrayAttribute> booleanArrayAttributes;

  static {
    // hack to force ProGuard to consider BooleanArrayAttribute used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(BooleanArrayAttribute.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<BooleanAttribute> booleanAttributes;

  static {
    // hack to force ProGuard to consider BooleanAttribute used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(BooleanAttribute.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<DateArrayAttribute> dateArrayAttributes;

  static {
    // hack to force ProGuard to consider DateArrayAttribute used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(DateArrayAttribute.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<DateAttribute> dateAttributes;

  static {
    // hack to force ProGuard to consider DateAttribute used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(DateAttribute.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String email;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<FloatArrayAttribute> floatArrayAttributes;

  static {
    // hack to force ProGuard to consider FloatArrayAttribute used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(FloatArrayAttribute.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<FloatAttribute> floatAttributes;

  static {
    // hack to force ProGuard to consider FloatAttribute used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(FloatAttribute.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String googleUserId;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<IntegerArrayAttribute> integerArrayAttributes;

  static {
    // hack to force ProGuard to consider IntegerArrayAttribute used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(IntegerArrayAttribute.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<IntegerAttribute> integerAttributes;

  static {
    // hack to force ProGuard to consider IntegerAttribute used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(IntegerAttribute.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Boolean isActive;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String name;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String pictureURL;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String secondName;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<StringArrayAttribute> stringArrayAttributes;

  static {
    // hack to force ProGuard to consider StringArrayAttribute used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(StringArrayAttribute.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<StringAttribute> stringAttributes;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String surname;

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getActive() {
    return active;
  }

  /**
   * @param active active or {@code null} for none
   */
  public Victim setActive(java.lang.Boolean active) {
    this.active = active;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<java.lang.String> getAllAttributeKeys() {
    return allAttributeKeys;
  }

  /**
   * @param allAttributeKeys allAttributeKeys or {@code null} for none
   */
  public Victim setAllAttributeKeys(java.util.List<java.lang.String> allAttributeKeys) {
    this.allAttributeKeys = allAttributeKeys;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<IAttribute> getAllAttributes() {
    return allAttributes;
  }

  /**
   * @param allAttributes allAttributes or {@code null} for none
   */
  public Victim setAllAttributes(java.util.List<IAttribute> allAttributes) {
    this.allAttributes = allAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<BooleanArrayAttribute> getBooleanArrayAttributes() {
    return booleanArrayAttributes;
  }

  /**
   * @param booleanArrayAttributes booleanArrayAttributes or {@code null} for none
   */
  public Victim setBooleanArrayAttributes(java.util.List<BooleanArrayAttribute> booleanArrayAttributes) {
    this.booleanArrayAttributes = booleanArrayAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<BooleanAttribute> getBooleanAttributes() {
    return booleanAttributes;
  }

  /**
   * @param booleanAttributes booleanAttributes or {@code null} for none
   */
  public Victim setBooleanAttributes(java.util.List<BooleanAttribute> booleanAttributes) {
    this.booleanAttributes = booleanAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<DateArrayAttribute> getDateArrayAttributes() {
    return dateArrayAttributes;
  }

  /**
   * @param dateArrayAttributes dateArrayAttributes or {@code null} for none
   */
  public Victim setDateArrayAttributes(java.util.List<DateArrayAttribute> dateArrayAttributes) {
    this.dateArrayAttributes = dateArrayAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<DateAttribute> getDateAttributes() {
    return dateAttributes;
  }

  /**
   * @param dateAttributes dateAttributes or {@code null} for none
   */
  public Victim setDateAttributes(java.util.List<DateAttribute> dateAttributes) {
    this.dateAttributes = dateAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getEmail() {
    return email;
  }

  /**
   * @param email email or {@code null} for none
   */
  public Victim setEmail(java.lang.String email) {
    this.email = email;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<FloatArrayAttribute> getFloatArrayAttributes() {
    return floatArrayAttributes;
  }

  /**
   * @param floatArrayAttributes floatArrayAttributes or {@code null} for none
   */
  public Victim setFloatArrayAttributes(java.util.List<FloatArrayAttribute> floatArrayAttributes) {
    this.floatArrayAttributes = floatArrayAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<FloatAttribute> getFloatAttributes() {
    return floatAttributes;
  }

  /**
   * @param floatAttributes floatAttributes or {@code null} for none
   */
  public Victim setFloatAttributes(java.util.List<FloatAttribute> floatAttributes) {
    this.floatAttributes = floatAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getGoogleUserId() {
    return googleUserId;
  }

  /**
   * @param googleUserId googleUserId or {@code null} for none
   */
  public Victim setGoogleUserId(java.lang.String googleUserId) {
    this.googleUserId = googleUserId;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public Victim setId(java.lang.String id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<IntegerArrayAttribute> getIntegerArrayAttributes() {
    return integerArrayAttributes;
  }

  /**
   * @param integerArrayAttributes integerArrayAttributes or {@code null} for none
   */
  public Victim setIntegerArrayAttributes(java.util.List<IntegerArrayAttribute> integerArrayAttributes) {
    this.integerArrayAttributes = integerArrayAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<IntegerAttribute> getIntegerAttributes() {
    return integerAttributes;
  }

  /**
   * @param integerAttributes integerAttributes or {@code null} for none
   */
  public Victim setIntegerAttributes(java.util.List<IntegerAttribute> integerAttributes) {
    this.integerAttributes = integerAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Boolean getIsActive() {
    return isActive;
  }

  /**
   * @param isActive isActive or {@code null} for none
   */
  public Victim setIsActive(java.lang.Boolean isActive) {
    this.isActive = isActive;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getName() {
    return name;
  }

  /**
   * @param name name or {@code null} for none
   */
  public Victim setName(java.lang.String name) {
    this.name = name;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getPictureURL() {
    return pictureURL;
  }

  /**
   * @param pictureURL pictureURL or {@code null} for none
   */
  public Victim setPictureURL(java.lang.String pictureURL) {
    this.pictureURL = pictureURL;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getSecondName() {
    return secondName;
  }

  /**
   * @param secondName secondName or {@code null} for none
   */
  public Victim setSecondName(java.lang.String secondName) {
    this.secondName = secondName;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<StringArrayAttribute> getStringArrayAttributes() {
    return stringArrayAttributes;
  }

  /**
   * @param stringArrayAttributes stringArrayAttributes or {@code null} for none
   */
  public Victim setStringArrayAttributes(java.util.List<StringArrayAttribute> stringArrayAttributes) {
    this.stringArrayAttributes = stringArrayAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<StringAttribute> getStringAttributes() {
    return stringAttributes;
  }

  /**
   * @param stringAttributes stringAttributes or {@code null} for none
   */
  public Victim setStringAttributes(java.util.List<StringAttribute> stringAttributes) {
    this.stringAttributes = stringAttributes;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getSurname() {
    return surname;
  }

  /**
   * @param surname surname or {@code null} for none
   */
  public Victim setSurname(java.lang.String surname) {
    this.surname = surname;
    return this;
  }

  @Override
  public Victim set(String fieldName, Object value) {
    return (Victim) super.set(fieldName, value);
  }

  @Override
  public Victim clone() {
    return (Victim) super.clone();
  }

}
