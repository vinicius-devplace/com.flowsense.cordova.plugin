#import <UIKit/UIKit.h>

@interface Util_fs : NSObject

+(void) AddData:(NSString *)key :(NSString *)data;
+(NSString *) GetData:(NSString *)key;
+(void) AddBOOL:(NSString *)key :(NSNumber *)value;
+(BOOL) GetBOOL:(NSString *)key;
+(void)AddDouble:(NSString *)key :(double)value;
+(double)GetDouble:(NSString *)key;
+(NSNumber *)GetBooleanNullable:(NSString *)key;
+(NSNumber *)GetDoubleNullable:(NSString *)key;
+(void)AddFailedData:(NSString *)key :(NSData *)value;
+(NSData *)GetFailedData:(NSString *)key;
+(void)AddPushDict:(NSString *)key :(NSDictionary *)value;
+(NSDictionary *)GetPushDict:(NSString *)key;

@end
