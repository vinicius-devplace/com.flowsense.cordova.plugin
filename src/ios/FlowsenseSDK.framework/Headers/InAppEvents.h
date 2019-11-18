#import <Foundation/Foundation.h>

@interface InAppEvents : NSObject

+(void) sendEventWithName:(NSString *)eventName values:(NSDictionary *)map;

@end
