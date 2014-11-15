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
 * (build: 2014-07-22 21:53:01 UTC)
 * on 2014-10-23 at 10:15:57 UTC 
 * Modify at your own risk.
 */

package epusp.pcs.os.workflow.emcallworkflowendpoint.model;

/**
 * Model definition for EmergencyCall.
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
public final class EmergencyCall extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private com.google.api.client.util.DateTime begin;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String emergencyCallLifecycle;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private com.google.api.client.util.DateTime end;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key @com.google.api.client.json.JsonString
  private java.lang.Long id;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private Position lastVictimPosition;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String monitor;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<VehicleOnCall> vehicles;

  static {
    // hack to force ProGuard to consider VehicleOnCall used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(VehicleOnCall.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String victimEmail;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.Integer victimPositionSize;

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<Position> victimPositions;

  /**
   * @return value or {@code null} for none
   */
  public com.google.api.client.util.DateTime getBegin() {
    return begin;
  }

  /**
   * @param begin begin or {@code null} for none
   */
  public EmergencyCall setBegin(com.google.api.client.util.DateTime begin) {
    this.begin = begin;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getEmergencyCallLifecycle() {
    return emergencyCallLifecycle;
  }

  /**
   * @param emergencyCallLifecycle emergencyCallLifecycle or {@code null} for none
   */
  public EmergencyCall setEmergencyCallLifecycle(java.lang.String emergencyCallLifecycle) {
    this.emergencyCallLifecycle = emergencyCallLifecycle;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public com.google.api.client.util.DateTime getEnd() {
    return end;
  }

  /**
   * @param end end or {@code null} for none
   */
  public EmergencyCall setEnd(com.google.api.client.util.DateTime end) {
    this.end = end;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Long getId() {
    return id;
  }

  /**
   * @param id id or {@code null} for none
   */
  public EmergencyCall setId(java.lang.Long id) {
    this.id = id;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public Position getLastVictimPosition() {
    return lastVictimPosition;
  }

  /**
   * @param lastVictimPosition lastVictimPosition or {@code null} for none
   */
  public EmergencyCall setLastVictimPosition(Position lastVictimPosition) {
    this.lastVictimPosition = lastVictimPosition;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getMonitor() {
    return monitor;
  }

  /**
   * @param monitor monitor or {@code null} for none
   */
  public EmergencyCall setMonitor(java.lang.String monitor) {
    this.monitor = monitor;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<VehicleOnCall> getVehicles() {
    return vehicles;
  }

  /**
   * @param vehicles vehicles or {@code null} for none
   */
  public EmergencyCall setVehicles(java.util.List<VehicleOnCall> vehicles) {
    this.vehicles = vehicles;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getVictimEmail() {
    return victimEmail;
  }

  /**
   * @param victimEmail victimEmail or {@code null} for none
   */
  public EmergencyCall setVictimEmail(java.lang.String victimEmail) {
    this.victimEmail = victimEmail;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.Integer getVictimPositionSize() {
    return victimPositionSize;
  }

  /**
   * @param victimPositionSize victimPositionSize or {@code null} for none
   */
  public EmergencyCall setVictimPositionSize(java.lang.Integer victimPositionSize) {
    this.victimPositionSize = victimPositionSize;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<Position> getVictimPositions() {
    return victimPositions;
  }

  /**
   * @param victimPositions victimPositions or {@code null} for none
   */
  public EmergencyCall setVictimPositions(java.util.List<Position> victimPositions) {
    this.victimPositions = victimPositions;
    return this;
  }

  @Override
  public EmergencyCall set(String fieldName, Object value) {
    return (EmergencyCall) super.set(fieldName, value);
  }

  @Override
  public EmergencyCall clone() {
    return (EmergencyCall) super.clone();
  }

}