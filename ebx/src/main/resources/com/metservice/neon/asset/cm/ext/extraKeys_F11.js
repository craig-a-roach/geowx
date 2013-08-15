function() {
	var scroller = editor.getScrollerElement();
	if (scroller.className.search(/\bCodeMirror-fullscreen\b/) === -1) {
		scroller.className += " CodeMirror-fullscreen";
		scroller.style.height = "100%";
		scroller.style.width = "100%";
	} else {
		scroller.className = scroller.className.replace(" CodeMirror-fullscreen", "");
		scroller.style.height = '';
		scroller.style.width = '';
	}
	editor.refresh();
}
	