var classNames = require('classnames');
var React = require('react');
var ReactDOM = require('react-dom');
var $ = require('jquery');

echoFrame=React.createClass({
  render:function(){
    return (<div>hello world</div>)
  }
})

ReactDOM.render(
  <echoFrame />,
  document.getElementById('container')
);
