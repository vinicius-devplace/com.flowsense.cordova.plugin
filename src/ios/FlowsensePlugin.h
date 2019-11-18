#import <Cordova/CDV.h>
#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface FlowsensePlugin : CDVPlugin

- (void)startSDK_fs:(CDVInvokedUrlCommand*)command;
- (void)startPushiOS:(CDVInvokedUrlCommand*)command;
- (void)updatePartnerUserId_fs:(CDVInvokedUrlCommand*)command;
- (void)listenPush:(CDVInvokedUrlCommand*)command;
+ (void)sendEvent:(NSMutableDictionary*)dictionary;
- (void)updateKeyValues:(CDVInvokedUrlCommand*)command;
- (void)updateKeyValueBoolean:(CDVInvokedUrlCommand*)command;
- (void)updateKeyValuesDate:(CDVInvokedUrlCommand*)command;
- (void)inAppEvent:(CDVInvokedUrlCommand*)command;
- (void)registerPush:(CDVInvokedUrlCommand*)command;
+ (void)sendPushToken:(NSMutableDictionary*)dictionary;

@end