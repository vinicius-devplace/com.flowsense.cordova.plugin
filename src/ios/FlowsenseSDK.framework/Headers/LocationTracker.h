#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import "LocationShareModel.h"
#import <UIKit/UIKit.h>
#import "DBGeofences.h"

@interface LocationTracker : NSObject <CLLocationManagerDelegate>

@property (nonatomic) CLLocationCoordinate2D myLastLocation;

@property (strong,nonatomic) LocationShareModel * shareModel;
@property (strong,nonatomic) NSMutableArray * insideGeofencesWithId;
@property (strong,nonatomic) NSTimer * timer;

@property (nonatomic) NSDate* distPast;
@property (nonatomic) NSDate* distFuture;

+ (CLLocationManager *)sharedLocationManager;

- (void) startLocationTracking;
- (void) restartUpdates;
- (void) PostJsonLocation:(NSString *)latitude :(NSString *)longitude :(NSDate *)date_arr :(NSDate *)date_dep :(double)accuracy;
- (void) sendEvent:(NSString *)latitude :(NSString *)longitude :(NSString *)dateArr :(NSString *)dateDep :(NSString *)duration :(double)accuracy :(int)points;
- (void) sendCheckin:(NSMutableDictionary *) dict;
- (void) sendDeparture:(NSDictionary *) dict;

//- (void) sendEventForce;
//- (void) sendCheckInForce;

@end
