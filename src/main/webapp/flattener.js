// Expecting dojo.xd.js to already be included
// Author timp
// November 2010

var startDate = new Date();
var start = startDate.getTime();
var outstandingAsynchCalls = 0;
var columns = new Array();
var values = new Array();
var followableLinkTypes = new Array();
followableLinkTypes["http://www.cggh.org/2010/chassis/terms/studyInfo"] = true;

function flattenAtomEntry(uri) {
  startDate = new Date();
  start = startDate.getTime();
  outstandingAsynchCalls = 0;
  columns = new Array();
  values = new Array();
  try {
    loadXMLDoc("",columns, values, uri);
  } catch (e) {
      var alertText = "Error: ";
      if(e.lineNumber) 
        alertText += " line " + e.lineNumber + " ";
      if (e.description) 
        alertText += e.description;
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

  deferred.addCallback(function(xmlDoc, ioargs) {
      flatten(pathToNode + xmlDoc.documentElement.nodeName + "[1]", columns, values, xmlDoc.documentElement);
      outstandingAsynchCalls--;  
      setDocumentContent();
    });
  deferred.addErrback(function(error) {
      console.debug(error);
    });
  
}

function flatten(pathToValue, columns, values, element) { 
  var kids = element.childNodes;
  if (kids.length == 1 && element.childNodes[0].nodeType == 3) {
    // element is a leaf
    var textContent = element.childNodes[0];
    if (trim(textContent.nodeValue) != "") { 
      columns.push(pathToValue);
      values.push(textContent.nodeValue);
    }
  } else {
    // element is not a leaf, recurse
        
    var counts = {};
      
    for (var i = 0; i < kids.length; i++){
      var kid = kids[i];
      var kidName = kid.nodeName;

      if (kidName == "#text" && kid.nodeValue) { 
        if(trim(kid.nodeValue) != "" ) 
            throw new Error("found text when kids length =  " + kids.length);
      } else if (kidName == undefined) {
          throw new Error("undefined at " + i + " when kids length =  " + kids.length);
      } else  if (kidName == "atom:link") {
        var rel = kid.getAttribute("rel"); 
        if (followableLinkTypes[rel]) { 
          var linkUrl = kid.getAttribute("href");
          // We could wrap pathToValue in another atom:link here
          // but then we would need to keep track of number of links
          loadXMLDoc(pathToValue + "/", columns, values, linkUrl)
        }
      } else {
        if (counts[kidName])
          counts[kidName] = counts[kidName]+1;
        else 
          counts[kidName] = 1;
        
        var newContext = pathToValue + "/" + kidName + "[" + counts[kidName] + "]";
        flatten(newContext, columns, values, kid);
  
      }
    }
  }
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
    return false; // IE does not allow navigation to data urls
  if(/chrome/i.test(navigator.userAgent) ) 
    return false; // Chrome seems to fail as well
  return true;  
}

