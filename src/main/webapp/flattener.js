// Expecting dojo.xd.js to already be included
// Author timp
// November 2010

var logOutput = "";
var startDate = new Date();
var start = startDate.getTime();
var outstandingAsynchCalls = 0;
var columns = new Array();
var values = new Array();


function load(uri) {
  logOutput = "";
  startDate = new Date();
  start = startDate.getTime();
  outstandingAsynchCalls = 0;
  columns = new Array();
  values = new Array();
  try {
    log("start:" + start);
    loadXMLDoc("",columns, values, uri);
    log("end:" + sinceStart());
  } catch (e) {
      var alertText = "Error: ";
      if(e.lineNumber) 
        alertText + " line " + e.lineNumber + " ";
      if (e.description) 
        alertText + e.description;
      else
        alertText += e;
      alert (alertText);
  }
}
function setDocumentContent() { 
  if (outstandingAsynchCalls == 0) {
    if (isDataUriSchemeSupported()) { 
      window.location.href='data:text/csv;charset=utf-8,' + 
        encodeURIComponent(quotedJoin(columns) + "\n" + quotedJoin(values) + "\n"); 
    // This also works, both methods require popup unblocking
    //  var win = window.open(
    //      'data:text/csv;charset=utf-8,' + 
    //       encodeURIComponent(quotedJoin(columns) + "\n" + quotedJoin(values) + "\n"),
    //    'jsTest'
    //   );
    } else {
      var win = window.open();
      var csv = win.document;
      csv.open("text/csv", "replace");
      csv.write("<pre>" + quotedJoin(columns) + "\n" + quotedJoin(values) + "\n</pre>\n");
      csv.close();
    } 
  } 
  
}
function quotedJoin(arrayIn) {
  var returnString = "";
  for (var i = 0; i < arrayIn.length; i++) {
    if (returnString != "") 
      returnString = returnString + ",";
    returnString = returnString + '"' + arrayIn[i] + '"';  
  }
  return returnString;
}
function loadXMLDoc(pathToNode, columns, values, urlIn) {
  outstandingAsynchCalls++;
  var xhrArgs = {
    url: urlIn,
    preventCache: true,
    handleAs: "xml"
  };
  var deferred = dojo.xhrGet(xhrArgs);

  //On success we'll process the doc and generate the JavaScript model
  deferred.addCallback(function(xmlDoc, ioargs) {
    flatten(pathToNode, columns, values, xmlDoc.documentElement);
    outstandingAsynchCalls--;  
    setDocumentContent();
    }
  );
  deferred.addErrback(function(error) {
    console.debug(error);
  });
  
}

function flatten(pathToValue, columns, values, element) { 
  //alert("Columns:" + columns.length);
  var kids = element.childNodes;
  if (kids.length == 1 && element.childNodes[0].nodeType == 3) {
    // element is a leaf
    // If there are no Child Elements then 
    // the first child will be a text one
    var textContent = element.childNodes[0];
    if (!textContent) {
      throw new Error("Node zero undefined ");
    } else {
      if (trim(textContent.nodeValue) != "") { 
        columns.push(pathToValue);
        values.push(textContent.nodeValue);
      } 
    }
  } else {
    // element is not a leaf, recurse
    var counts = {};
      
    for (var i = 0; i < kids.length; i++){
      var kid = kids[i];
      var name = kid.nodeName;

      if (name == "#text" && kid.nodeValue) { 
        if(trim(kid.nodeValue) != "" ) 
            throw new Error("found text when kids length =  " + kids.length);
      } else if (name == undefined) {
          throw new Error("undefined at " + i + " when kids length =  " + kids.length);
      } else  if (name == "atom:link") {
        if (kid.getAttribute("rel") == "http://www.cggh.org/2010/chassis/terms/studyInfo") { 
          var linkUrl = kid.getAttribute("href");
          loadXMLDoc(pathToValue, columns, values, linkUrl)
        }
      } else {
        if (counts[name])
          counts[name] = counts[name]+1;
        else 
          counts[name] = 1;
        
        var newContext = pathToValue == "" ? "" : pathToValue + ".";
        newContext = newContext + name + "/" + counts[name];
        flatten(newContext, columns, values, kid);
  
      }
    }
  }
}

function log(text){ 
  message("Log", text);
}
function message(type, text) { 
    logOutput = logOutput + "<br/>\n" + type + ":" + text;        
}
function dump(node) {
  var returnString = node.nodeName + "(" + node.nodeType+")=" + node.nodeValue + "\n";
  var kids = node.childNodes;
  returnString = returnString + "length:"  + kids.length +"\n";
  for (var i = 0; i < kids.length; i++){
    returnString = returnString + "--" +  i + ")" + kids[i].nodeName + "(" + kids[i].nodeType + ")=" + kids[i].nodeValue + "\n";
  }
  return returnString; 
}
function trim(str) {
  if (str==null) return "";
  var  str = str.replace(/^\s\s*/, ''),
       ws = /\s/,
       i = str.length;
  while (ws.test(str.charAt(--i)));
  return str.slice(0, i + 1);
}
function isDataUriSchemeSupported() { 
  if(/msie/i.test(navigator.userAgent) ) 
    return false; // IE does nto allow navigation to data urls
  if(/chrome/i.test(navigator.userAgent) ) 
      return false; // Chrome seems to fail
  return true;  
}
function sinceStart() {
  var nowDate = new Date();
  return  nowDate.getTime() - start; 
}

