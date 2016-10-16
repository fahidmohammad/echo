var http = require('http');
var extend = require('xtend');
var parseString = require('xml2js').parseString;
var util={}
util.download=function(url, callback) {
  http.get(encodeURI(url), function(res) {
    var data = "";
    res.on('data', function (chunk) {
      data += chunk;
    });
    res.on("end", function() {
      callback(data);
    });
  }).on("error", function(err) {
    console.log(err);
    callback(null);
  });
}

util.download=function(url, callback) {
  http.get(encodeURI(url), function(res) {
    var data = "";
    res.on('data', function (chunk) {
      data += chunk;
    });
    res.on("end", function() {
      callback(data);
    });
  }).on("error", function(err) {
    console.log(err);
    callback(null);
  });
}

util.giataFactSheetConvertJson=function(json){
  var obj={}
  var _t={}
  obj.giataId=json.item[0].$.giataId
  obj.name=json.item[0].name[0]
  //obj.street=json.item[0].street[0]
  //obj.city=json.item[0].city[0]
  //obj.country=json.item[0].country[0]
  obj.factSheetLastUpdate=json.item[0].factsheet[0].$.lastUpdate
  var tempobj=json.item[0].factsheet[0].sections[0].section
  for(var key in tempobj){
    obj[tempobj[key].$.name]={}
    var _arr=tempobj[key].facts[0].fact
    for(var _keyj in _arr){
      var data=_arr[_keyj].value[0]
      if(data._!=undefined && data.$.fee!=undefined) data={'_':data._,'fee':data.$.fee}
      obj[tempobj[key].$.name][_arr[_keyj].$.name]=data
    }
  }
  for(key in obj.distances){
    obj.distances[key]=obj.distances[key]._+obj.distances[key].$.unit
  }  
  return obj
}

util.giataTextConvertJson=function(json){
  var obj={}
  var _t={}
  //obj.giataId=json.item[0].$.giataId
  obj.textLastUpdate=json.item[0].texts[0].text[0].$.lastUpdate
  obj.lang=json.item[0].texts[0].text[0].$.lang
  var tempobj=json.item[0].texts[0].text[0].sections[0].section
  for(var key in tempobj){
    obj[tempobj[key].title]=tempobj[key].para[0]
  }
  return obj
}

util.giataHotelListConvertJson=function(json){
  var arr=[]
  var temp=json.items[0].item
  for(var i in temp){
    arr[i]=temp[i].$.giataId
  }
  return arr
}

util.giataMultiCodePropertiesConvertJson=function(json){
  var obj={}
  //This is done after fact sheets so redundant records are not recorded.
  obj.giataId=json.properties.property[0].$.giataId
  obj.propertyLastUpdate=json.properties.property[0].$.lastUpdate
  var _o={}
  if(json.properties.property[0].addresses) {
    _o=json.properties.property[0].addresses[0].address[0]
    _o.addressLine=json.properties.property[0].addresses[0].address[0].addressLine.map(function(e){return e._}).join(', ')
    for(var i in _o){
      if(i!='addressLine')
      _o[i]=_o[i][0]
    }
    obj.address=_o
  }
  if(json.properties.property[0].emails) obj.email=json.properties.property[0].emails[0].email[0]
  if(json.properties.property[0].urls) obj.url=json.properties.property[0].urls[0].url[0]
  obj.latitude=json.properties.property[0].geoCodes[0].geoCode[0].latitude
  obj.longitude=json.properties.property[0].geoCodes[0].geoCode[0].longitude
  if(json.properties.property[0].propertyCodes) obj.providers=json.properties.property[0].propertyCodes[0].provider
  _o={}
  for(i in obj.providers){
    var ele=obj.providers[i]
    _o[ele.$.providerCode]={
      providerType:ele.$.providerType,
      code:ele.code.map(function(e){
        if(e.value[0]._) {
          return e.value.map(function(f){
            return {value: f._, name: f.$.name}
          })
        }else{
          return e.value[0]
        }

      })
    }
  }

  obj.providers=_o
  return obj
}

util.downloadHotelData=function(id,callback){
  var factsheetURLbase='http://mhg|hackathon.com:GjfdNNVq@ghgml.giatamedia.com/webservice/rest/1.0/factsheets/'
  var textURLbase='http://mhg|hackathon.com:GjfdNNVq@ghgml.giatamedia.com/webservice/rest/1.0/texts/en/'
  var multicodeURL_base='http://multicodes|hackathon.com:xYpzHfjJ@multicodes.giatamedia.com/webservice/rest/1.0/properties/'
  util.download(factsheetURLbase+id,
    function(factsheet){
      parseString(factsheet, function (err, result) {
        //remapping of the values to json
        var data=util.giataFactSheetConvertJson(result.result)
        util.download(textURLbase+id,
          function(text){
            parseString(text, function (err, result) {
              data.text=util.giataTextConvertJson(result.result)
              util.download(multicodeURL_base+id,
                function(text){
                  parseString(text, function (err, result) {
                    data = extend(data,util.giataMultiCodePropertiesConvertJson(result));
                    callback(data)        
                  })    
                })       
            })    
          })
      });
    })
}

util.resort=function(original,request){
  for(i in original){
    if(request.indexOf(original[i])==-1){
      request.push(original[i])
    }
  }
  return request
}
module.exports = util;