#import <UIKit/UIKit.h>

@interface Cordova : NSObject

+(void) startLocationTracker;
+(void) StartFlowsenseService:(NSString *)partnerToken;
+(void) StartFlowsensePushService:(NSDictionary *) launchOptions;
+(void) updatePartnerUserIdiOS:(NSString *) userId;
+(void) downloadGeofences;

@end
