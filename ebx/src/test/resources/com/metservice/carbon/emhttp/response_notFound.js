AUTHOR('craig');
PURPOSE('Unit Test');

throw new HttpError('not found', 'Missing Thing');
/*
 * Equivalent to
throw new HttpError(404, 'Missing Thing');
 */
