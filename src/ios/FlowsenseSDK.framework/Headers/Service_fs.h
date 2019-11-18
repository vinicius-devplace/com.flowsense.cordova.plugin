#import <UIKit/UIKit.h>
#import <UserNotifications/UserNotifications.h>

@interface Service_fs : NSObject

NS_ASSUME_NONNULL_BEGIN
+(void) StartFlowsenseService:(NSString *)partnerToken;
+(void) StartFlowsenseService:(NSString *)partnerToken :(BOOL) startNow;

//Push Services
+(void) StartFlowsensePushService:(NSDictionary *) launchOptions;
+(void) includeMediaAttachmentWithRequest:(UNNotificationRequest *)request mutableContent:(UNMutableNotificationContent *)bestAttemptContent contentHandler:(void (^)(UNNotificationContent * _Nonnull))contentHandler;

+(void) sendPushToken:(NSData *)token;
+(NSDictionary *) getPushExtras;

+(void) StartMonitoringLocation;
+(void) updatePartnerUserIdiOS:(NSString *) userId;

+(void) updateGeofences;
+(NSArray *) getStoredGeofences;
+(NSArray *) getKeyValues;
NS_ASSUME_NONNULL_END


@end
