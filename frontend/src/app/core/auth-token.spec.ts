import { TestBed } from '@angular/core/testing';

import { AuthToken } from './auth-token';

describe('AuthToken', () => {
  let service: AuthToken;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthToken);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
