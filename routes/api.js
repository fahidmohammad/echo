var express = require('express')
var extend = require('xtend')
var parseString = require('xml2js').parseString
var string_score = require('string_score')
var mongoose = require('mongoose')
var equal = require('deep-equal')
var request = require('superagent')
var util = require('./util')

var router = express.Router()

var dburl = 'mongodb://localhost/echo'
var modelSchema = mongoose.Schema({
  data: Object,
  id: String
})
var model = mongoose.model('Model', modelSchema)

/* GET home page. */
router.get('/', function (req, res, next) {
  res.render('index', { title: 'Express' })
})

/* POST AI text */
router.post('/text', function (req, res, next) {
  var _data = extend(req.query, req.body)
  console.log(_data)
  res.send(_data)
})


router.post('/search', function (req, res, next) {
  // We also allow limit of sorting : _data.limit=3  
  var _data = extend(req.query, req.body, req.params)
  console.log(_data)
  if (!_data.limit) _data.limit = 5
  if (_data.message) {
    request
      .get('https://api.api.ai/v1/query?query=' + _data.message + '&lang=en')
      .set('Authorization', 'Bearer d4eb846fb8d84ebeaaa2a9abba6ec5b1')
      .end(function (err, result) {
        result = result.body.result
        console.log(result)
        var action = result.action || result.metadata.intentName
        if (action) {
          mongoose.connect(dburl)
          mongoose.connection
            .on('error', console.error.bind(console, 'connection error:'))
            .once('open', function () {
              model.find({}, function (err, results) {
                // we do custom filtering here
                if (err) {
                  mongoose.disconnect()
                  res.send({'error': err})
                  return false
                }
                results = results.filter(function (e) {
                  if (action == 'hotel.findSimilar') {
                    var arr=[]
                    if(result.parameters['hotel-name']){
                      arr.push(e.data.name.toLowerCase().score(result.parameters['hotel-name'].toLowerCase()) > 0.5)
                    }
                    if(result.parameters['hotel-nearby']){
                      var nearby = result.parameters['hotel-nearby']
                      if(typeof(nearby)==typeof('dummy')){
                        var matched= e.data.text.Location ? e.data.text.Location.match(new RegExp(nearby,'i')) : false   
                        arr.push(matched && matched[0]!==undefined)
                      }else{
                        for(var i in nearby){
                          var matched= e.data.text.Location ? e.data.text.Location.match(new RegExp(nearby[i],'i')) : false
                          arr.push(matched && matched[0]!==undefined)
                        }
                      }
                    }
                    return arr.reduce(function (a, b) {return a & b})
                  } else if (action == 'hotel.hasServices' && result.parameters['hotel-extra-services'] && result.parameters['hotel-nearby'] || _data.context_id) {
                    var service = result.parameters['hotel-extra-services']
                    var nearby = result.parameters['hotel-nearby']
                    var arr = [true]
                    if(_data.context_id && _data.context_id!=e.id) return false
                    if (service.indexOf('pool') != -1) {
                      arr.push((e.data.sports_entertainment && parseInt(e.data.sports_entertainment.sports_numberofpools, 10) >= 1))
                    }
                    if (service.indexOf('air con') != -1 && e.data.facilities && e.data.facilities.facility_aircon) {
                      arr.push(e.data.facilities.facility_aircon == 'true' || e.data.facilities.facility_aircon._ == 'true')
                    }
                    if(service.indexOf('breakfast') != -1){
                      arr.push(e.data.meals && (e.data.meals.meals_breakfastserved=='true' || e.data.meals.meals_breakfastbuffet == 'true'))
                    }
                    if(service.indexOf('dinner') != -1){
                      arr.push(e.data.meals && (e.data.meals.meals_dinnerbuffet=='true' || e.data.meals.meals_dinnercarte == 'true'))
                    }
                    if(service.indexOf('bar') != -1){
                      arr.push(e.data.facilities && (e.data.facilities.facility_bars=='true' || (e.data.room_facilities && e.data.room_facilities.room_minibar == 'true')))
                    }
                    if(service.indexOf('showers') != -1){
                      arr.push(e.data.room_facilities && e.data.room_facilities.room_shower=='true')
                    }
                    if(typeof(nearby)==typeof('dummy')){
                      var matched= e.data.text.Location ? e.data.text.Location.match(new RegExp(nearby,'i')) : false                    
                      arr.push(matched && matched[0]!==undefined)
                    }else{
                      for(var i in nearby){
                        var matched= e.data.text.Location ? e.data.text.Location.match(new RegExp(nearby[i],'i')) : false                    
                        arr.push(matched && matched[0]!==undefined)
                      }
                    }
                    return arr.reduce(function (a, b) {return a & b})
                  }
                }).map(function (e) {
                  return _data.full=='true' ? e : {'name': e.data.name, 'id': e.id}
                })
                //if (_data.limit) resultsarr = resultsarr.slice(0, _data.limit)
                results.push({'action':'hotel.findSimilar'})
                mongoose.disconnect()
                res.send( _data.context_id ? results.length>0 : results)
                return
              })
            })
        }else {
          mongoose.disconnect()
          res.send({'error': 'NoActionError'})
          return false
        }
      })
  }else if (_data.context_action) {
    if (_data.context_action == 'hotel.findSimilar') {
      //This should be changed to another ip
      var url = 'http://192.168.1.78:3000/api/search/' + _data.context_id + '/similar?context_action='+_data.context_action
      if(_data.matches) url+='&matches='+_data.matches
      if(_data.limit) url+='&limit='+_data.limit
      if(_data.full) url+='&full='+_data.full
      request
        .get(url)
        .end(function (err, result) {
          res.send(result.body)
        })
    }
  }else {
    res.send({'error': 'NothingFoundError'})
    return true
  }
})

router.get('/search/:giataid/similar', function (req, res, next) {
  var _data = extend(req.query, req.body, req.params)
  console.log(_data)
  // We allow deep searching for conditions in _data.matches
  // example : _data.matches='sports_entertainment.sports_pooloutdoor|meals.meals_breakfastserved'
  // We also allow priority sorting : _data.priority = Location|Facilities|Rooms|Sports/Entertainment|Meals|Payment
  // We also allow limit of sorting : _data.limit=3
  if (!_data.limit) _data.limit = 5
  // Here we only compare the text portions.
  mongoose.connect(dburl)
  mongoose.connection
    .on('error', console.error.bind(console, 'connection error:'))
    .once('open', function () {
      model.findOne({'id': _data.giataid}, function (err, result) {
        if (err) {
          mongoose.disconnect()
          res.send({'error': err})
          return false
        }
        var seedHotel = result.data
        // We use this seed and filter out the others.
        model.find({}, function (err, results) {
          // We do custom filters
          if (err) {
            mongoose.disconnect()
            res.send({'error': err})
            return false
          }
          results = !_data.matches ? results : results.filter(function (ele) {
            var d = ele.data
            var filters = _data.matches.split('|').map(function (_) {return _.split('.')})
            for (var j in filters) {
              if (d[filters[j][0]]) {
                if (filters[j].length == 1) {
                  return equal(d[filters[j][0]], seedHotel[filters[j][0]])
                }else {
                  return d[filters[j][0]][filters[j][1]] == seedHotel[filters[j][0]][filters[j][1]]
                }
              }
            }
          })

          console.log(results.length)
          sortingarr = ['Location', 'Facilities', 'Rooms', 'Sports/Entertainment', 'Meals', 'Payment']
          sortingarr = !_data.priority ? sortingarr : util.resort(_data.priority.split('|'), sortingarr)
          resultsarr = results.map(function (ele) {
            var t = ele.data.text
            var arr = sortingarr.map(function (ele) {
              return seedHotel.text[ele].score(t[ele] + '', 0.5)
            })
            return (ele.id == result.id) ? null : {id: ele.id,score: arr}
          }).sort()
          if (_data.limit) resultsarr = resultsarr.slice(0, _data.limit)
          resultsarr = resultsarr.map(function (_) {
            console.log(_)
            return _.id})
          for (var i in results) {
            var needle = resultsarr.indexOf(results[i].id)
            if (needle > -1) resultsarr[needle] = results[i]
          }
          console.log('h')
          resultsarr=resultsarr.map(function (e) {
            return _data.full=='true' ? e : {'name': e.data.name, 'id': e.id}
          })
          //result = _data.full=='true' ? result : {'name': result.data.name, 'id': result.id}
          mongoose.disconnect()
          
          resultsarr.push({'action':_data.context_action})
          res.send({original: result, similar: resultsarr})
          //res.send(resultsarr)
          return false
        })
      })
    })
})

/* GET update db to current GIATA data */
// */ We don't need this anymore after we have populated our database
router.get('/updatedb', function (req, res, next) {
  // res.render('index', { title: 'Express' })
  console.log('hellow')
  var singaporeURL = 'http://mhg|hackathon.com:GjfdNNVq@ghgml.giatamedia.com/webservice/rest/1.0/texts/en/?country=SG'
  util.download(singaporeURL, function (data) {
    parseString(data, function (err, result) {
      mongoose.connect(dburl)
      mongoose.connection
        .on('error', console.error.bind(console, 'connection error:'))
        .once('open', function () {
          if (err) {
            mongoose.disconnect()
            res.send({'error': err})
            return false
          }
          console.log('start')
          function storeModels (data, count) {
            if (count == undefined) count = 0
            console.log('Now saving:' + data[count] + '\n')
            if (!data[count]) {
              mongoose.disconnect()
              res.send('Completed.')
              return true
            }
            util.downloadHotelData(data[count], function (result) {
              var query = {id: data[count]}
              var updatedata = {id: data[count],data: result}
              var options = { upsert: true, new: true }
              model.findOneAndUpdate(query, updatedata, options, function (err, _result) {
                if (err) {
                  mongoose.disconnect()
                  res.send({'error': err})
                  return false
                }
                console.log('Saved ' + data[count] + ' ; ' + count + '\n')
                if (count < data.length) {
                  storeModels(data, count + 1)
                }else {
                  mongoose.disconnect()
                  res.send('Completed.')
                  return true
                }
              })
            })
          }
          storeModels(util.giataHotelListConvertJson(result.result))
        })
    })
  })
})

router.get('/test', function (req, res, next) {
  var id = '10759'
  // util.downloadHotelData(id,function(data){
  //  res.send(data)
  // })
  var multicodeURL_base = 'http://multicodes|hackathon.com:xYpzHfjJ@multicodes.giatamedia.com/webservice/rest/1.0/properties/'
  util.download(multicodeURL_base + id,
    function (text) {
      parseString(text, function (err, result) {
        // data = extend(data,util.giataMultiCodePropertiesConvertJson(result))
        // res.send(util.giataMultiCodePropertiesConvertJson(result))  
        res.send(result)
      })
    })
})
// */
module.exports = router
