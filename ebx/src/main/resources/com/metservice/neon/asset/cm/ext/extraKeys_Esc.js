function() {
	var scroller = editor.getScrollerElement();
	if (scroller.className.search(/\bCodeMirror-fullscreen\b/) !== -1) {
		scroller.className = scroller.className.replace(" CodeMirror-fullscreen", "");
		scroller.style.height = '';
		scroller.style.width = '';
		editor.refresh();
	}
}
