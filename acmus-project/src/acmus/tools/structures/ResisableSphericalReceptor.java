//package acmus.tools.structures;
//
//public final class ResisableSphericalReceptor implements Receptor{
//	private Vector center;
//	private float radius;
//	private float soundSpeed;
//	private float airAbsortionCoefficient;
//	private float riInterval;
//	private EnergeticSimulatedImpulseResponse sir;
//
//	public ResisableSphericalReceptor(Vector center, float radius, float soundSpeed, float airAbsortionCoefficient) {
//		this.center = center;
//		this.radius = radius;
//		this.soundSpeed = soundSpeed;
//		this.airAbsortionCoefficient = airAbsortionCoefficient;
//	
//		riInterval = 2 * radius / soundSpeed;
//		sir = new EnergeticSimulatedImpulseResponse(riInterval); 
//	}
//	
//	public boolean intercept(Vector origin, Vector ray, float pathLength, float rayLength, float rayEnergy){
//
//		Vector oc = center.sub(origin);
//		float l2oc = oc.dotProduct(oc);
//		float tca = oc.dotProduct(ray);
//
//
//		if (tca >= 0) {
//			float t2hc = radius * radius - l2oc + tca * tca;
//			
//			if (t2hc > 0) { // ray intercepts receptor
//				float lengthInterception = tca - (float) Math.sqrt(t2hc);
//				
//				if(lengthInterception < pathLength) { // test if ray hits a wall before receptor
//					float finalRayLength= rayLength + lengthInterception;
//					
//					float timeInterception = finalRayLength / soundSpeed;
//					float finalRayEnergy = rayEnergy  * (float) Math.pow(Math.E, -1 * airAbsortionCoefficient * finalRayLength);
//					
//					sir.addValue(timeInterception, finalRayEnergy);
//					
//					return true;
//				}
//			}
//		}
//		
//		return false;
//	}
//}
