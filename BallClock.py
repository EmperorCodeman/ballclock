from collections import  deque
from copy import deepcopy
import sys
class BallClock:
    '''
    Gain a understanding of what a ball clock is by watching the video below before digesting program
    https://www.youtube.com/watch?v=UHBHCsrqYMw
    In my code the que is order specific unlike in video
    Min return to que first, then 5min, then hour. Last used first into que 
    
    Operation of the Ball Clock -Every minute, the least recently used ball is removed from the queue of
    balls at the bottom of the clock, elevated, then deposited on the minute indicator track, which is able to
    hold four balls. When a fifth ball rolls on to the minute indicator track, its weight causes the track to tilt.
    The four balls already on the track run back down to join the queue of balls waiting at the bottom in
    reverse order of their original addition to the minutes track. The fifth ball, which caused the tilt, rolls on
    down to the five-minute indicator track. This track holds eleven balls. The twelfth ball carried over from
    the minutes causes the five-minute track to tilt, returning the eleven balls to the queue, again in reverse
    order of their addition. The twelfth ball rolls down to the hour indicator. The hour indicator also holds
    eleven balls, but has one extra fixed ball which is always present so that counting the balls in the hour
    indicator will yield an hour in the range one to twelve. The twelfth ball carried over from the five-minute
    indicator causes the hour indicator to tilt, returning the eleven free balls to the queue, in reverse order,
    before the twelfth ball itself also returns to the queue.
    '''
    def __init__(self, numberOfBalls):
        assert (27 <= numberOfBalls and numberOfBalls <= 127)
        self.numberOfBalls = numberOfBalls  #currently never used outside local scope
        self.min = []   #Minute Track, 5 values possible
        self.fiveMin = []   #Five Minute Track, 12 Values possible
        self.hour = []  #Hour Track, 11 Values possible
        self.cycleCount = 0 #counting cycles is a efficient way of storing time delta
        self.que = deque([i for i in range(numberOfBalls)])
        self.cycleHash = deepcopy(self.que) #when the que becomes identical to its starting configuration a cycle is complete
        self.cycleDuration = None   #[days,hour,minutes]
        self.hoursSinceStartOfACycle = 0    #resent at each start of a cycle
    def checkForCycleCompletion(self):
        '''check if cycle is completed by comparing
                original que to current que each time they
                are the same after start a cycle is complete
        '''
        if self.que == self.cycleHash:
            self.cycleCount += 1
            if self.cycleCount > 1:  # CycleDuration requires hours since start of cycle to init
                self.hoursSinceStartOfACycle = 0  # prevent hitting max int by reseting for none python3 implementation
            # defensive programming practice
            # logically a cycle should result in all the tracks being empty
            assert (len(self.hour) + len(self.fiveMin) + len(self.min) == 0)
            return True
        return False
    def addToQue(self, balls):
        for ball in balls:
            self.que.append(ball)
            if self.checkForCycleCompletion() and self.cycleDuration is None:    #if cycle duration is none then we are yet to set the cycle len
                #set cycle len
                self.cycleDuration = self.getTimeElapsed()
                #A partial day is counted as a full day by criteria
                if self.cycleDuration[1] == 12:self.cycleDuration[0] += 1
                self.hoursSinceStartOfACycle = 0  # prevent hitting max int by reseting
    def addMinute(self, ball):
        #add ball from que to min track
        self.min.append(ball)
        '''if 5 balls in min track then add last ball to 5 min track
        then add remaining 4 balls to que'''
        if len(self.min) >= 5:
            routToFiveMin = self.min.pop()
            self.min.reverse()  #instruction specify to reverse order
            self.addToQue(self.min)
            self.min.clear()
            self.add5Min(routToFiveMin)
    def add5Min(self,ball):
        '''if 12 balls in 5min track then add last ball to hour track
           then add remaining 11 balls to que
        '''
        self.fiveMin.append(ball)
        if len(self.fiveMin) >= 12:
            routToHourTrack = self.fiveMin.pop()
            self.fiveMin.reverse()
            self.addToQue(self.fiveMin)
            self.fiveMin.clear()
            self.addHour(routToHourTrack)
    def addHour(self,ball):
        '''if 13 balls including virtual fixed ball then drop all balls into que'''
        self.hour.append(ball)
        if len(self.hour) >= 12:
            hour = deepcopy(self.hour)#so clear can be called before addToQue and Assertion Possible
            routToQue = hour.pop()
            self.hour.clear()
            hour.reverse()
            self.addToQue(hour)
            self.addToQue([routToQue])
            self.hoursSinceStartOfACycle += 12
    def run(self, duration):
        #duration in minutes
        days,hours = divmod(duration,60*24) #divmod return quotient and remainder as truple
        hours, minutes = divmod(hours,60)
        print("Running " + str(days) + ":Days, " + str(hours) + ":Hours, " + str(minutes) + ":Minutes")
        for min in range(duration):
            self.addMinute(self.que.popleft())
    def getCycleDuration(self):
        runTime = 10000 #minutes
        while self.cycleDuration is None:
            self.run(runTime)
            runTime *= 10
        return self.cycleDuration
    def getTimeElapsed(self):
        time = []
        time.append(self.hoursSinceStartOfACycle // 24)  # truncate for days
        time.append((self.hoursSinceStartOfACycle % 24) + len(self.hour))  # remainder for hour of day
        time.append(len(self.min) + 5*len(self.fiveMin)) # min should be 0
        if self.cycleDuration:  #tabulate total time running including past cycles
            time[0] += self.cycleCount * self.cycleDuration[0]#days
            time[1] += self.cycleCount * self.cycleDuration[1]#hours
            time[2] += self.cycleCount * self.cycleDuration[2]#minutes
        return time
    def getCurrentTime(self):
        time = self.getTimeElapsed()
        time[1] += 1   #The clock starts at 1 hour. pm/am not specified
        return time
    def display(self,arg):
        if arg == "time elapsed":
            timeRunning = self.getTimeElapsed()
            print("\nTime Elapsed:\n"
                + "\tDays: " + str(timeRunning[0])
                + "\n\tHours: " + str(timeRunning[1])
                + "\n\tMinutes: " + str(timeRunning[2])
            )
        if arg == "days to complete cycle":
            print("\nDays to Complete Cycle: " + str(self.cycleDuration[0]))
        if arg == "state of the tracks":    #to be done in jason
            print("\nState of the Tracks:")
            print("\tHour Track: " + str((len(self.hour) + 1)))  # Add fixed virtual hour
            print("\tFive Minute Track: " + str(len(self.fiveMin)))
            print("\tMinute Track: " + str(len(self.min)))
if __name__ == "__main__":
    if len(sys.argv) == 1 or len(sys.argv) > 3:
        raise ValueError("Must pass One or two integers, first value between 27-127, if second value added it must be a positive integer")
    numberOfBalls = int(sys.argv[1])
    ballClock = BallClock(numberOfBalls)
    if len(sys.argv) == 3:
        durationToRunClock = int(sys.argv[2])
        assert (durationToRunClock > 0)
        ballClock.run(durationToRunClock)
        print(ballClock.display("state of the tracks"))
    else:
        ballClock.getCycleDuration()
        ballClock.display("days to complete cycle")
'''
#Test Set
#30 balls cycle after 15 days.
#45 balls cycle after 378 days.
'''