#import "FlowsensePlugin.h"
#import <Cordova/CDV.h>
#import <FlowsenseSDK/Cordova.h>
#import <FlowsenseSDK/KeyValuesManager.h>
#import <FlowsenseSDK/InAppEvents.h>


@implementation FlowsensePlugin

static NSDictionary* launchOptions = nil;
static NSString* _callbackId, *_pushCallbackId;
static FlowsensePlugin *_context, *_pushContext;

+(void)load {
    NSLog(@"Flowsense plugin initialized");
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(didFinishLaunching:)
                                                 name:UIApplicationDidFinishLaunchingNotification
                                               object:nil];
}

+(void)didFinishLaunching:(NSNotification*)notification {
    launchOptions = notification.userInfo;
    if (launchOptions == nil) {
        //launchOptions is nil when not start because of notification or url open
        launchOptions = [NSDictionary dictionary];
    }
    [Cordova StartFlowsensePushService:launchOptions];
    [Cordova startLocationTracker];
}

+ (void)sendEvent:(NSMutableDictionary*)dictionary{
    NSLog(@"Send Event Called");
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dictionary];
    [pluginResult setKeepCallbackAsBool:YES];
    [_context.commandDelegate sendPluginResult:pluginResult callbackId:_callbackId];
    NSLog(@"%@", _callbackId);
}

- (void)listenPush:(CDVInvokedUrlCommand*)command {
    _callbackId = command.callbackId;
    NSLog(@"Set CallbackID %@", _callbackId);
    _context = self;

    NSUserDefaults *preferences = [NSUserDefaults standardUserDefaults];
    if (([preferences objectForKey:@"FSChangeViewFlowsense"] == nil) ? NO : [preferences boolForKey:@"FSChangeViewFlowsense"]){
        NSString *view = ([preferences objectForKey:@"FSViewToChangeFlowsense"] == nil) ? @"" : [preferences stringForKey:@"FSViewToChangeFlowsense"];
        NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:1];
        [dict setObject:view forKey:@"action"];
        [preferences setValue:[NSNumber numberWithInt:0] forKey:@"FSChangeViewFlowsense"];
        [preferences synchronize];
        [FlowsensePlugin sendEvent:dict];
    }


    
}

- (void)startSDK_fs:(CDVInvokedUrlCommand*)command {
    NSLog(@"Starting Flowsense SDK");
    CDVPluginResult* pluginResult;
    //NSLog(@"ARGUMENTOS SAO: %@", command.arguments);
    [Cordova StartFlowsenseService:[command.arguments objectAtIndex:0]];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)startPushiOS:(CDVInvokedUrlCommand*)command {
    NSLog(@"Flowsense Push Service Enabled");
    [Cordova StartFlowsensePushService:launchOptions];
}

- (void)updatePartnerUserId_fs:(CDVInvokedUrlCommand*)command {
    NSLog(@"Flowsense Update Partner User Id");
    CDVPluginResult* pluginResult;
    [Cordova updatePartnerUserIdiOS:[command.arguments objectAtIndex:0]];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)updateKeyValues:(CDVInvokedUrlCommand*)command {
    @try{
        if ([[command.arguments objectAtIndex:1] isKindOfClass:[NSString class]])
        {
            [KeyValuesManager setKeyValue:[command.arguments objectAtIndex:0] valueString:[command.arguments objectAtIndex:1]];
        }
        else if ([[command.arguments objectAtIndex:1] isKindOfClass:[NSNumber class]]){
            [KeyValuesManager setKeyValue:[command.arguments objectAtIndex:0] valueDouble:[[command.arguments objectAtIndex:1] doubleValue]];
        }
    } @catch (NSException *exception){
        NSLog(@"%@", exception);
    }
}

- (void)updateKeyValueBoolean:(CDVInvokedUrlCommand*)command {
    @try{
        [KeyValuesManager setKeyValue:[command.arguments objectAtIndex:0] valueBoolean:[[command.arguments objectAtIndex:1] boolValue]];
    } @catch (NSException *exception){
        NSLog(@"%@", exception);
    }
}

- (void)updateKeyValuesDate:(CDVInvokedUrlCommand*)command {
    @try{
        [KeyValuesManager setKeyValue:[command.arguments objectAtIndex:0] valueDate:[NSDate dateWithTimeIntervalSince1970:[[command.arguments objectAtIndex:1] longValue]/1000]];
    } @catch (NSException *exception){
        NSLog(@"%@", exception);
    }
}

- (void)commitKeyValues:(CDVInvokedUrlCommand*)command {
    @try{
        [KeyValuesManager commitChanges];
        CDVPluginResult* pluginResult;
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } @catch (NSException *exception){
        NSLog(@"%@", exception);
    }
}

- (void)inAppEvent:(CDVInvokedUrlCommand*)command {
    @try{
        NSString *eventName = [command.arguments objectAtIndex:0];
        NSDictionary *map = [command.arguments objectAtIndex:1];
        [InAppEvents sendEventWithName:eventName values:map];
    }
    @catch (NSException *exception){
        NSLog(@"%@", exception);
    }
}

- (void)registerPush:(CDVInvokedUrlCommand*)command {
    _pushCallbackId = command.callbackId;
    NSLog(@"Set PushCallbackID %@", _pushCallbackId);
    _pushContext = self;

    NSUserDefaults *preferences = [NSUserDefaults standardUserDefaults];
    NSString *token = ([preferences objectForKey:@"FSPushToken"] == nil) ? @"" : [preferences stringForKey:@"FSPushToken"];
    if (![token isEqualToString:@""])
    {
        NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithCapacity:1];
        [dict setObject:token forKey:@"token"];
        [FlowsensePlugin sendPushToken:dict];
    }
        
    
}

+ (void)sendPushToken:(NSMutableDictionary*)dictionary{
    NSLog(@"Send Push Token Called");
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dictionary];
    [pluginResult setKeepCallbackAsBool:YES];
    [_pushContext.commandDelegate sendPluginResult:pluginResult callbackId:_pushCallbackId];
    NSLog(@"Push Callback ID: %@", _pushCallbackId);
}

+(void)sendPushEvent:(NSDictionary*)dictionary{
    NSLog(@"Send Push Event Called");
    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:@{@"additionalData": dictionary}];
    [pluginResult setKeepCallbackAsBool:YES];
    [_pushContext.commandDelegate sendPluginResult:pluginResult callbackId:_pushCallbackId];
    NSLog(@"Push Callback ID: %@", _pushCallbackId);
}

@end